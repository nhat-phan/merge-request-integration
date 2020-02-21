package net.ntworld.mergeRequest.request

import net.ntworld.foundation.Request
import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequest.response.ReplyCommentResponse

interface ReplyCommentRequest : Request<ReplyCommentResponse> {
    val providerId: String

    val mergeRequestId: String

    val repliedComment: Comment

    val body: String

    companion object
}