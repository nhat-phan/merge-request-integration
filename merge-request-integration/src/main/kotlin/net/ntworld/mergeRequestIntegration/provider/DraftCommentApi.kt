package net.ntworld.mergeRequestIntegration.provider

import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequest.CommentPosition
import net.ntworld.mergeRequest.Project
import net.ntworld.mergeRequest.api.CommentApi
import net.ntworld.mergeRequest.api.DraftCommentStorage

class DraftCommentApi(private val api: CommentApi, private val storage: DraftCommentStorage): CommentApi {
    override fun getAll(project: Project, mergeRequestId: String): List<Comment> {
        val result = mutableListOf<Comment>()
        result.addAll(api.getAll(project, mergeRequestId))
        result.addAll(storage.getAll(project, mergeRequestId))

        return result
    }

    override fun create(
        project: Project,
        mergeRequestId: String,
        body: String,
        position: CommentPosition?,
        isDraft: Boolean
    ): String? {
        if (isDraft) {
            return storage.create(project, mergeRequestId, body, position)
        }
        return api.create(project, mergeRequestId, body, position, false)
    }

    override fun reply(project: Project, mergeRequestId: String, repliedComment: Comment, body: String): String? {
        return api.reply(project, mergeRequestId, repliedComment, body)
    }

    override fun delete(project: Project, mergeRequestId: String, comment: Comment) {
        if (comment.isDraft) {
            return storage.delete(project, mergeRequestId, comment)
        }
        return api.delete(project, mergeRequestId, comment)
    }

    override fun resolve(project: Project, mergeRequestId: String, comment: Comment) {
        if (!comment.isDraft) {
            return api.resolve(project, mergeRequestId, comment)
        }
    }

    override fun unresolve(project: Project, mergeRequestId: String, comment: Comment) {
        if (!comment.isDraft) {
            return api.unresolve(project, mergeRequestId, comment)
        }
    }

    override fun getDraftCount(project: Project, mergeRequestId: String): Int {
        return storage.getAll(project, mergeRequestId).count()
    }

    override fun publishAllDraftComments(project: Project, mergeRequestId: String) {

    }
}