package net.ntworld.mergeRequest.request

import net.ntworld.foundation.Request
import net.ntworld.mergeRequest.response.PublishAllCommentsResponse

interface PublishAllCommentsRequest : Request<PublishAllCommentsResponse> {
    val providerId: String

    val mergeRequestId: String

    companion object
}