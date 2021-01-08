package net.ntworld.mergeRequest.response

import net.ntworld.foundation.Response

interface PublishAllCommentsResponse : Response {
    val success: Boolean

    companion object
}