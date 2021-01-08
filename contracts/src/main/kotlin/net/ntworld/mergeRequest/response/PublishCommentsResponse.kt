package net.ntworld.mergeRequest.response

import net.ntworld.foundation.Response

interface PublishCommentsResponse : Response {
    val success: Boolean

    companion object
}