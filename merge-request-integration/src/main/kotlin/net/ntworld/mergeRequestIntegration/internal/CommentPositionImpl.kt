package net.ntworld.mergeRequestIntegration.internal

import net.ntworld.mergeRequest.CommentPosition
import net.ntworld.mergeRequest.CommentPositionChangeType
import net.ntworld.mergeRequest.CommentPositionSource

data class CommentPositionImpl(
    override val baseHash: String,
    override val startHash: String,
    override val headHash: String,
    override val oldPath: String?,
    override val newPath: String?,
    override val oldLine: Int?,
    override val newLine: Int?,
    override val source: CommentPositionSource,
    override val changeType: CommentPositionChangeType
): CommentPosition