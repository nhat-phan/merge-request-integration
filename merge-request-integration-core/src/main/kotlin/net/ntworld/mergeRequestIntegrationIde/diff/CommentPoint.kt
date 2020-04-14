package net.ntworld.mergeRequestIntegrationIde.diff

import net.ntworld.mergeRequest.Comment

data class CommentPoint(
    val line: Int,
    val comment: Comment
) {
    val id: String = comment.id
}