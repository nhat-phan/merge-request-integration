package net.ntworld.mergeRequestIntegration.requestHandler

import net.ntworld.foundation.Handler
import net.ntworld.foundation.RequestHandler
import net.ntworld.mergeRequest.request.ReplyCommentRequest
import net.ntworld.mergeRequest.response.ReplyCommentResponse
import net.ntworld.mergeRequestIntegration.ProviderStorage
import net.ntworld.mergeRequestIntegration.make
import net.ntworld.mergeRequestIntegration.provider.ProviderException

@Handler
class ReplyCommentRequestHandler(
    private val providerStorage: ProviderStorage
) : RequestHandler<ReplyCommentRequest, ReplyCommentResponse> {

    override fun handle(request: ReplyCommentRequest): ReplyCommentResponse {
        val (data, api) = providerStorage.findOrFail(request.providerId)
        return try {
            val createdCommentId = api.comment.reply(
                project = data.project,
                mergeRequestId = request.mergeRequestId,
                repliedComment = request.repliedComment,
                body = request.body
            )
            ReplyCommentResponse.make(error = null, createdCommentId = createdCommentId)
        } catch (exception: ProviderException) {
            ReplyCommentResponse.make(error = exception.error, createdCommentId = null)
        }
    }

}