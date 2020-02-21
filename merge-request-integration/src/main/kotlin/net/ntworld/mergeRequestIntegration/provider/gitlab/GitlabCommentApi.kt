package net.ntworld.mergeRequestIntegration.provider.gitlab

import net.ntworld.foundation.Infrastructure
import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequest.CommentPosition
import net.ntworld.mergeRequest.CommentPositionSource
import net.ntworld.mergeRequest.Project
import net.ntworld.mergeRequest.api.ApiCredentials
import net.ntworld.mergeRequest.api.CommentApi
import net.ntworld.mergeRequestIntegration.provider.ProviderException
import net.ntworld.mergeRequestIntegration.provider.gitlab.command.*
import net.ntworld.mergeRequestIntegration.provider.gitlab.request.GitlabCreateNoteRequest
import net.ntworld.mergeRequestIntegration.provider.gitlab.request.GitlabGetMRCommentsRequest
import net.ntworld.mergeRequestIntegration.provider.gitlab.request.GitlabGetMRDiscussionsRequest
import net.ntworld.mergeRequestIntegration.provider.gitlab.request.GitlabReplyNoteRequest
import net.ntworld.mergeRequestIntegration.provider.gitlab.transformer.GitlabCommentTransformer
import net.ntworld.mergeRequestIntegration.provider.gitlab.transformer.GitlabDiscussionTransformer
import org.gitlab4j.api.models.Position

class GitlabCommentApi(
    private val infrastructure: Infrastructure,
    private val credentials: ApiCredentials
) : CommentApi {
    override fun getAll(project: Project, mergeRequestId: String): List<Comment> {
        val request = GitlabGetMRDiscussionsRequest(
            credentials = credentials,
            mergeRequestInternalId = mergeRequestId.toInt()
        )
        val response = infrastructure.serviceBus() process request ifError {
            throw Exception(it.message)
        }
        val comments = mutableListOf<Comment>()
        response.discussions.forEach {
            comments.addAll(GitlabDiscussionTransformer.transform(it))
        }
        return comments
    }

    private fun getAllGraphQL(project: Project, mergeRequestId: String): List<Comment> {
        val fullPath = findProjectFullPath(project)
        val comments = mutableListOf<Comment>()
        var endCursor = ""
        do {
            val request = GitlabGetMRCommentsRequest(
                credentials = credentials,
                projectFullPath = fullPath,
                mergeRequestInternalId = mergeRequestId.toInt(),
                endCursor = endCursor
            )
            val response = infrastructure.serviceBus() process request ifError {
                throw Exception(it.message)
            }

            val payload = response.payload
            if (null !== payload) {
                endCursor = payload.data.project.mergeRequest.notes.pageInfo.endCursor
                payload.data.project.mergeRequest.notes.nodes.forEach {
                    if (!it.system) {
                        comments.add(GitlabCommentTransformer.transform(it))
                    }
                }
            }
        } while (null === payload || payload.data.project.mergeRequest.notes.pageInfo.hasNextPage)

        return comments
    }

    override fun create(project: Project, mergeRequestId: String, body: String, position: CommentPosition?): String? {
        val createdCommentId = if (null === position) {
            val request = GitlabCreateNoteRequest(
                credentials = credentials,
                mergeRequestInternalId = mergeRequestId.toInt(),
                body = body,
                position = null
            )
            val response = infrastructure.serviceBus() process request ifError {
                throw ProviderException(it)
            }
            response.createdCommentId
        } else {
            tryCreateNoteCommand(
                mergeRequestInternalId = mergeRequestId.toInt(),
                body = body,
                position = buildFirstAttemptPosition(position)
            ) {
                tryCreateNoteCommand(
                    mergeRequestInternalId = mergeRequestId.toInt(),
                    body = body,
                    position = buildSecondAttemptPosition(position)
                ) {
                    tryCreateNoteCommand(
                        mergeRequestInternalId = mergeRequestId.toInt(),
                        body = body,
                        position = buildThirdAttemptPosition(position)
                    ) {
                        throw it
                    }
                }
            }
        }

        return if (createdCommentId == 0) null else createdCommentId.toString()
    }

    private fun buildFirstAttemptPosition(position: CommentPosition): Position {
        val model = makePosition(position)
        when (position.source) {
            CommentPositionSource.UNKNOWN -> {
                model.oldLine = null
                model.newLine = position.newLine
            }
            CommentPositionSource.SIDE_BY_SIDE_LEFT -> {
                model.oldLine = position.oldLine
                model.newLine = null
            }
            CommentPositionSource.SIDE_BY_SIDE_RIGHT -> {
                model.oldLine = null
                model.newLine = position.newLine
            }
            CommentPositionSource.UNIFIED -> {
                model.oldLine = null
                model.newLine = if (null === position.newLine || position.newLine!! < 0) null else position.newLine
            }
        }
        return model
    }

    private fun buildSecondAttemptPosition(position: CommentPosition): Position {
        val model = makePosition(position)
        model.oldLine = position.oldLine
        model.newLine = position.newLine
        return model
    }

    private fun buildThirdAttemptPosition(position: CommentPosition): Position {
        val model = makePosition(position)
        when (position.source) {
            CommentPositionSource.UNKNOWN -> {
                model.oldLine = position.oldLine
                model.newLine = null
            }
            CommentPositionSource.SIDE_BY_SIDE_LEFT -> {
                model.oldLine = position.oldLine
                model.newLine = null
            }
            CommentPositionSource.SIDE_BY_SIDE_RIGHT -> {
                model.oldLine = null
                model.newLine = position.newLine
            }
            CommentPositionSource.UNIFIED -> {
                model.oldLine = if (null === position.oldLine || position.oldLine!! < 0) null else position.oldLine
                model.newLine = null
            }
        }
        return model
    }

    private fun makePosition(position: CommentPosition): Position {
        val model = Position()
        model.baseSha = position.baseHash
        model.headSha = position.headHash
        model.startSha = position.startHash
        model.oldPath = position.oldPath
        model.newPath = position.newPath
        model.positionType = Position.PositionType.TEXT
        return model
    }

    private fun tryCreateNoteCommand(
        mergeRequestInternalId: Int,
        body: String,
        position: Position?,
        failed: (exception: Exception) -> Int
    ): Int {
        val request = GitlabCreateNoteRequest(
            credentials = credentials,
            mergeRequestInternalId = mergeRequestInternalId,
            body = body,
            position = position
        )
        val out = infrastructure.serviceBus() process request

        return if (out.hasError()) {
            failed(ProviderException(out.getResponse().error!!))
        } else {
            out.getResponse().createdCommentId
        }
    }

    override fun reply(project: Project, mergeRequestId: String, repliedComment: Comment, body: String): String? {
        val request = GitlabReplyNoteRequest(
            credentials = credentials,
            mergeRequestInternalId = mergeRequestId.toInt(),
            discussionId = repliedComment.parentId,
            noteId = repliedComment.id.toInt(),
            body = body
        )

        val response = infrastructure.serviceBus() process request ifError {
            throw ProviderException(it)
        }
        return response.createdCommentId.toString()
    }

    override fun delete(project: Project, mergeRequestId: String, comment: Comment) {
        val command = GitlabDeleteNoteCommand(
            credentials = credentials,
            mergeRequestInternalId = mergeRequestId.toInt(),
            discussionId = comment.parentId,
            noteId = comment.id.toInt()
        )
        infrastructure.commandBus() process command
    }

    override fun resolve(project: Project, mergeRequestId: String, comment: Comment) {
        infrastructure.commandBus() process GitlabResolveNoteCommand(
            credentials = credentials,
            mergeRequestInternalId = mergeRequestId.toInt(),
            discussionId = comment.parentId,
            resolve = true
        )
    }

    override fun unresolve(project: Project, mergeRequestId: String, comment: Comment) {
        infrastructure.commandBus() process GitlabResolveNoteCommand(
            credentials = credentials,
            mergeRequestInternalId = mergeRequestId.toInt(),
            discussionId = comment.parentId,
            resolve = false
        )
    }

    private fun findProjectFullPath(project: Project): String {
        val url = project.url.replace(credentials.url, "")
        return if (url.startsWith("/")) url.substring(1) else url
    }
}