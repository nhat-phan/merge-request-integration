package net.ntworld.mergeRequest.response

import net.ntworld.foundation.Response

interface CreateCommentResponse : Response {
    val createdCommentId: String?

    companion object
}