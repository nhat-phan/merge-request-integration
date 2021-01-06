package net.ntworld.mergeRequestIntegration.provider.gitlab

import net.ntworld.foundation.Infrastructure
import net.ntworld.mergeRequest.*
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
import java.util.logging.Level
import java.util.logging.Logger

class GitlabCommentApi(
    private val infrastructure: Infrastructure,
    private val credentials: ApiCredentials
) : CommentApi {

    companion object {
        private val log: Logger = Logger.getLogger("GitlabCommentApi")
    }

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

    override fun create(
        project: Project,
        mergeRequestId: String,
        body: String,
        position: CommentPosition?,
        isDraft: Boolean
    ): String? {
        if (isDraft) {
            println(body)
            println(position)
            // TODO: do something
            return ""
        }

        val createdCommentId = if (null === position) {
            createGeneralComment(mergeRequestId, body)
        } else {
            createPositionComment(mergeRequestId, body, position)
        }
        return if (createdCommentId == 0) null else createdCommentId.toString()
    }

    private fun createGeneralComment(mergeRequestId: String, body: String): Int {
        val request = GitlabCreateNoteRequest(
            credentials = credentials,
            mergeRequestInternalId = mergeRequestId.toInt(),
            body = body,
            position = null
        )
        val response = infrastructure.serviceBus() process request ifError {
            throw ProviderException(it)
        }
        return response.createdCommentId
    }

    private fun createPositionComment(mergeRequestId: String, body: String, commentPosition: CommentPosition) : Int {
        val position = makePosition(commentPosition)
        if (commentPosition.changeType != CommentPositionChangeType.UNKNOWN) {
            if (commentPosition.source == CommentPositionSource.SIDE_BY_SIDE_LEFT) {
                position.newLine = null
                position.newPath = null
            }
            if (commentPosition.source == CommentPositionSource.SIDE_BY_SIDE_RIGHT) {
                position.oldLine = null
                position.oldPath = null
            }
        }
        val request = GitlabCreateNoteRequest(
            credentials = credentials,
            mergeRequestInternalId = mergeRequestId.toInt(),
            body = body,
            position = position
        )
        val out = infrastructure.serviceBus() process request
        if (out.hasError()) {
            throw ProviderException(out.getResponse().error!!)
        }
        return out.getResponse().createdCommentId
    }

    private fun makePosition(position: CommentPosition): Position {
        val model = Position()
        model.baseSha = position.baseHash
        model.headSha = position.headHash
        model.startSha = position.startHash
        model.oldPath = if (null === position.oldLine || position.oldLine!! < 0) null else position.oldPath
        model.newPath = if (null === position.newLine || position.newLine!! < 0) null else position.newPath
        model.oldLine = if (null === position.oldLine || position.oldLine!! < 0) null else position.oldLine
        model.newLine = if (null === position.newLine || position.newLine!! < 0) null else position.newLine
        model.positionType = Position.PositionType.TEXT
        return model
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

    override fun getDraftCount(project: Project, mergeRequestId: String): Int {
        throw Exception("Not implemented in GitlabCommentApi, see DraftCommentApi")
    }

    override fun publishAllDraftComments(project: Project, mergeRequestId: String) {
        throw Exception("Not implemented in GitlabCommentApi, see DraftCommentApi")
    }

    private fun findProjectFullPath(project: Project): String {
        val url = project.url.replace(credentials.url, "")
        return if (url.startsWith("/")) url.substring(1) else url
    }
}