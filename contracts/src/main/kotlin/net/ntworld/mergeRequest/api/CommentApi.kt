package net.ntworld.mergeRequest.api

import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequest.CommentPosition
import net.ntworld.mergeRequest.Project

interface CommentApi {
    fun find(project: Project, mergeRequestId: String, commentId: String): Comment?

    fun getAll(project: Project, mergeRequestId: String): List<Comment>

    fun create(project: Project, mergeRequestId: String, body: String, position: CommentPosition?)

    fun reply(project: Project, mergeRequestId: String, repliedComment: Comment, body: String)

    // fun update(projectId: String, mergeRequestId: String, comment: Comment): Boolean

    // fun delete(projectId: String, mergeRequestId: String, commentId: String): Boolean

    fun findOrFail(project: Project, mergeRequestId: String, commentId: String): Comment {
        val comment = find(project, mergeRequestId, commentId)
        if (null === comment) {
            throw Exception("Comment $commentId not found.")
        }
        return comment
    }
}