package net.ntworld.mergeRequest.api

import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequest.CommentPosition
import net.ntworld.mergeRequest.Project

interface CommentApi {
    fun getAll(project: Project, mergeRequestId: String): List<Comment>

    fun create(project: Project, mergeRequestId: String, body: String, position: CommentPosition?): String?

    fun reply(project: Project, mergeRequestId: String, repliedComment: Comment, body: String): String?

    fun delete(project: Project, mergeRequestId: String, comment: Comment)

    fun resolve(project: Project, mergeRequestId: String, comment: Comment)

    fun unresolve(project: Project, mergeRequestId: String, comment: Comment)

    // fun update(projectId: String, mergeRequestId: String, comment: Comment): Boolean
}