package net.ntworld.mergeRequest.response

import net.ntworld.foundation.Response

interface ReplyCommentResponse : Response {
    val createdCommentId: String?

    companion object
}