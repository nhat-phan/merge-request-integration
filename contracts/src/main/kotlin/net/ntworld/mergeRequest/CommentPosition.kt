package net.ntworld.mergeRequest

interface CommentPosition {
    val baseHash: String

    val startHash: String

    val headHash: String

    val oldPath: String?

    val newPath: String?

    val oldLine: Int?

    val newLine: Int?

    val source: CommentPositionSource

    companion object
}
