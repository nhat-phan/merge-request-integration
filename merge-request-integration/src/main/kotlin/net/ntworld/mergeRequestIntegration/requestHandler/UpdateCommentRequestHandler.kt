package net.ntworld.mergeRequestIntegration.requestHandler

import net.ntworld.foundation.Handler
import net.ntworld.foundation.RequestHandler
import net.ntworld.mergeRequest.request.UpdateCommentRequest
import net.ntworld.mergeRequest.response.UpdateCommentResponse
import net.ntworld.mergeRequestIntegration.ProviderStorage
import net.ntworld.mergeRequestIntegration.make
import net.ntworld.mergeRequestIntegration.provider.ProviderException

@Handler
class UpdateCommentRequestHandler(
    private val providerStorage: ProviderStorage
): RequestHandler<UpdateCommentRequest, UpdateCommentResponse> {
    override fun handle(request: UpdateCommentRequest): UpdateCommentResponse {
        val (data, api) = providerStorage.findOrFail(request.providerId)
        return try {
            api.comment.update(
                project = data.project,
                mergeRequestId = request.mergeRequestId,
                comment = request.comment,
                body = request.body
            )
            UpdateCommentResponse.make(error = null, commentId = request.comment.id)
        } catch (exception: ProviderException) {
            UpdateCommentResponse.make(error = exception.error, commentId = null)
        }
    }
}