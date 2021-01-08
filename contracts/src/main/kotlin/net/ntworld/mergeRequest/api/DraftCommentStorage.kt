package net.ntworld.mergeRequest.api

import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequest.CommentPosition
import net.ntworld.mergeRequest.Project

interface DraftCommentStorage {
    fun getAll(project: Project, mergeRequestId: String): List<Comment>

    fun findById(project: Project, mergeRequestId: String, commentId: String): Comment?

    fun create(project: Project, mergeRequestId: String, body: String, position: CommentPosition?): String?

    fun update(project: Project, mergeRequestId: String, comment: Comment, body: String)

    fun delete(project: Project, mergeRequestId: String, comment: Comment)
}