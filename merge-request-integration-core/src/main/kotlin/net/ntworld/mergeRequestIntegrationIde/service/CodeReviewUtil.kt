package net.ntworld.mergeRequestIntegrationIde.service

import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequest.CommentPosition

interface CodeReviewUtil {
    fun findCommentNodeData(comment: Comment) : CommentNodeData

    fun convertPositionToCommentNodeData(position: CommentPosition) : CommentNodeData

    interface CommentNodeData {
        val isGeneral: Boolean

        val fullPath: String

        val fileName: String

        val line: Int

        fun getHash(): String
    }
}