package net.ntworld.mergeRequestIntegration.provider.gitlab.requestHandler

import net.ntworld.foundation.Handler
import net.ntworld.foundation.RequestHandler
import net.ntworld.mergeRequestIntegration.provider.gitlab.GitlabFuelClient
import net.ntworld.mergeRequestIntegration.provider.gitlab.model.ReplyCommentPayload
import net.ntworld.mergeRequestIntegration.provider.gitlab.request.GitlabReplyNoteRequest
import net.ntworld.mergeRequestIntegration.provider.gitlab.response.GitlabReplyNoteResponse

@Handler
class GitlabReplyNoteRequestHandler : RequestHandler<GitlabReplyNoteRequest, GitlabReplyNoteResponse> {

    override fun handle(request: GitlabReplyNoteRequest) = GitlabFuelClient(
        request = request,
        execute = {
            val url = "${baseProjectUrl}/merge_requests/${request.mergeRequestInternalId}/discussions/${request.discussionId}/notes"
            val parameters = listOf(
                Pair("body", request.body)
            )
            val result = this.postJson(url = url, parameters = parameters)
            val payload = json.parse(ReplyCommentPayload.serializer(), result)

            GitlabReplyNoteResponse(error = null, createdCommentId = payload.id)
        },
        failed = {
            GitlabReplyNoteResponse(error = it, createdCommentId = 0)
        }
    )

}