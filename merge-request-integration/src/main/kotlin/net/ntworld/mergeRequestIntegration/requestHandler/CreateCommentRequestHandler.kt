package net.ntworld.mergeRequestIntegration.requestHandler

import net.ntworld.foundation.Handler
import net.ntworld.foundation.RequestHandler
import net.ntworld.mergeRequest.request.CreateCommentRequest
import net.ntworld.mergeRequest.response.CreateCommentResponse
import net.ntworld.mergeRequestIntegration.ApiProviderManager
import net.ntworld.mergeRequestIntegration.make
import net.ntworld.mergeRequestIntegration.provider.ProviderException

@Handler
class CreateCommentRequestHandler : RequestHandler<CreateCommentRequest, CreateCommentResponse> {

    override fun handle(request: CreateCommentRequest): CreateCommentResponse {
        val (data, api) = ApiProviderManager.findOrFail(request.providerId)
        return try {
            val createdCommentId = api.comment.create(
                project = data.project,
                mergeRequestId = request.mergeRequestId,
                body = request.body,
                position = request.position
            )
            CreateCommentResponse.make(error = null, createdCommentId = createdCommentId)
        } catch (exception: ProviderException) {
            CreateCommentResponse.make(error = exception.error, createdCommentId = null)
        }
    }

}