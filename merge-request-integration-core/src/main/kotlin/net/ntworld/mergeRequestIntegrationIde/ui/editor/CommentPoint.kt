package net.ntworld.mergeRequestIntegrationIde.ui.editor

import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequestIntegrationIde.service.CodeReviewManager

data class CommentPoint(
    val line: Int,
    val comment: Comment
) {
    val id: String = comment.id
}