package net.ntworld.mergeRequest.request

import net.ntworld.foundation.Request
import net.ntworld.mergeRequest.CommentPosition
import net.ntworld.mergeRequest.response.CreateCommentResponse

interface CreateCommentRequest : Request<CreateCommentResponse> {
    val providerId: String

    val mergeRequestId: String

    val body: String

    val position: CommentPosition?

    val isDraft: Boolean

    companion object
}
