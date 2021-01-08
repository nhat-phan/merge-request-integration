package net.ntworld.mergeRequestIntegration.requestHandler

import net.ntworld.foundation.Handler
import net.ntworld.foundation.RequestHandler
import net.ntworld.mergeRequest.request.PublishCommentsRequest
import net.ntworld.mergeRequest.response.PublishCommentsResponse
import net.ntworld.mergeRequestIntegration.ProviderStorage
import net.ntworld.mergeRequestIntegration.make
import net.ntworld.mergeRequestIntegration.provider.ProviderException

@Handler
class PublishCommentsRequestHandler(
    private val providerStorage: ProviderStorage
): RequestHandler<PublishCommentsRequest, PublishCommentsResponse> {

    override fun handle(request: PublishCommentsRequest): PublishCommentsResponse {
        val (data, api) = providerStorage.findOrFail(request.providerId)
        return try {
            api.comment.publishDraftComments(
                project = data.project,
                mergeRequestId = request.mergeRequestId,
                commentIds = request.draftCommentIds
            )
            PublishCommentsResponse.make(error = null, success = true)
        } catch (exception: ProviderException) {
            PublishCommentsResponse.make(error = exception.error, success = false)
        }
    }

}