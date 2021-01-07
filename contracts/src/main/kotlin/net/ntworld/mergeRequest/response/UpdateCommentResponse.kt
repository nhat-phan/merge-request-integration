package net.ntworld.mergeRequest.response

import net.ntworld.foundation.Response

interface UpdateCommentResponse : Response {
    val commentId: String?

    companion object
}