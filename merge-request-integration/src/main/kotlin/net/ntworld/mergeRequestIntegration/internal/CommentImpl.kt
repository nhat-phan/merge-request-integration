package net.ntworld.mergeRequestIntegration.internal

import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequest.CommentPosition
import net.ntworld.mergeRequest.DateTime
import net.ntworld.mergeRequest.UserInfo

data class CommentImpl(
    override val id: String,
    override val parentId: String,
    override val replyId: String,
    override val body: String,
    override val author: UserInfo,
    override val position: CommentPosition?,
    override val createdAt: DateTime,
    override val updatedAt: DateTime,
    override val resolvable: Boolean,
    override val resolved: Boolean,
    override val resolvedBy: UserInfo?,
    override val isDraft: Boolean
) : Comment {
}
