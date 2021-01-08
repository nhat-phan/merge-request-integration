package net.ntworld.mergeRequestIntegration.requestHandler

import net.ntworld.foundation.Handler
import net.ntworld.foundation.RequestHandler
import net.ntworld.mergeRequest.request.PublishAllCommentsRequest
import net.ntworld.mergeRequest.response.PublishAllCommentsResponse
import net.ntworld.mergeRequestIntegration.ProviderStorage
import net.ntworld.mergeRequestIntegration.make
import net.ntworld.mergeRequestIntegration.provider.ProviderException

@Handler
class PublishAllCommentsRequestHandler(
    private val providerStorage: ProviderStorage
): RequestHandler<PublishAllCommentsRequest, PublishAllCommentsResponse> {

    override fun handle(request: PublishAllCommentsRequest): PublishAllCommentsResponse {
        val (data, api) = providerStorage.findOrFail(request.providerId)
        return try {
            api.comment.publishAllDraftComments(
                project = data.project,
                mergeRequestId = request.mergeRequestId
            )
            PublishAllCommentsResponse.make(error = null, success = true)
        } catch (exception: ProviderException) {
            PublishAllCommentsResponse.make(error = exception.error, success = false)
        }
    }

}