package net.ntworld.mergeRequest.request

import net.ntworld.foundation.Request
import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequest.response.UpdateCommentResponse

interface UpdateCommentRequest : Request<UpdateCommentResponse> {
    val providerId: String

    val mergeRequestId: String

    val comment: Comment

    val body: String

    companion object
}