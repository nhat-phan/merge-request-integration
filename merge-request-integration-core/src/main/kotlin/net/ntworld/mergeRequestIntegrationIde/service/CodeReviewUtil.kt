package net.ntworld.mergeRequestIntegrationIde.service

import com.intellij.openapi.vcs.changes.Change
import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequest.CommentPosition

interface CodeReviewUtil {
    fun findCommentPosition(change: Change, line: Int, side: Side): CommentPosition

    fun findCommentNodeData(comment: Comment) : CommentNodeData

    fun convertPositionToCommentNodeData(position: CommentPosition) : CommentNodeData

    interface CommentNodeData {
        val isGeneral: Boolean

        val fullPath: String

        val fileName: String

        val line: Int

        fun getHash(): String
    }

    enum class Side {
        LEFT,
        RIGHT
    }
}