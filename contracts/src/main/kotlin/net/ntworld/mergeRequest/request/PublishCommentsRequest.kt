package net.ntworld.mergeRequest.request

import net.ntworld.foundation.Request
import net.ntworld.mergeRequest.response.PublishCommentsResponse

interface PublishCommentsRequest : Request<PublishCommentsResponse> {
    val providerId: String

    val mergeRequestId: String

    val draftCommentIds: List<String>

    companion object
}