package net.ntworld.mergeRequestIntegrationIde.internal

import com.intellij.openapi.vcs.changes.Change
import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequest.CommentPosition
import net.ntworld.mergeRequestIntegrationIde.service.CodeReviewUtil

object CodeReviewUtilImpl : CodeReviewUtil {

    override fun findCommentNodeData(comment: Comment): CodeReviewUtil.CommentNodeData {
        val position = comment.position
        if (null === position) {
            return CommentNodeDataImpl(isGeneral = true, fullPath = "", fileName = "General", line = 0)
        }
        return convertPositionToCommentNodeData(position)
    }

    override fun convertPositionToCommentNodeData(position: CommentPosition): CodeReviewUtil.CommentNodeData {
        val hasLeft = null !== position.oldLine && position.oldLine!! > 0
        val hasRight = null !== position.newLine && position.newLine!! > 0
        if (hasLeft && !hasRight) {
            return CommentNodeDataImpl(
                isGeneral = false,
                fullPath = position.oldPath!!,
                fileName = findFileName(position.oldPath!!),
                line = position.oldLine!!
            )
        }

        if (!hasLeft && hasRight) {
            return CommentNodeDataImpl(
                isGeneral = false,
                fullPath = position.newPath!!,
                fileName = findFileName(position.newPath!!),
                line = position.newLine!!
            )
        }

        // TODO: check for change case
        return CommentNodeDataImpl(
            isGeneral = false,
            fullPath = position.oldPath!!,
            fileName = findFileName(position.oldPath!!),
            line = position.oldLine!!
        )
    }

    private fun findFileName(path: String): String {
        return path.split("/").last()
    }
}