package net.ntworld.mergeRequest

interface Comment {
    val id: String

    val parentId: String

    val replyId: String

    val body: String

    val author: UserInfo

    val position: CommentPosition?

    val createdAt: DateTime

    val updatedAt: DateTime

    val resolvable: Boolean

    val resolved: Boolean

    val resolvedBy: UserInfo?

    val isDraft: Boolean

    companion object
}