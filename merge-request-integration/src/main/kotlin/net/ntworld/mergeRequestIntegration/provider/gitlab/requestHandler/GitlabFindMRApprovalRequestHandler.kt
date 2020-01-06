package net.ntworld.mergeRequestIntegration.provider.gitlab.requestHandler

import net.ntworld.foundation.Handler
import net.ntworld.foundation.RequestHandler
import net.ntworld.mergeRequestIntegration.provider.gitlab.GitlabFuelClient
import net.ntworld.mergeRequestIntegration.provider.gitlab.model.ApprovalModel
import net.ntworld.mergeRequestIntegration.provider.gitlab.request.GitlabFindMRApprovalRequest
import net.ntworld.mergeRequestIntegration.provider.gitlab.response.GitlabFindMRApprovalResponse

@Handler
class GitlabFindMRApprovalRequestHandler : RequestHandler<GitlabFindMRApprovalRequest, GitlabFindMRApprovalResponse> {
    override fun handle(request: GitlabFindMRApprovalRequest): GitlabFindMRApprovalResponse = GitlabFuelClient(
        request = request,
        execute = {
            val response = this.getJson(
                "${this.baseProjectUrl}/merge_requests/${request.mergeRequestInternalId}/approvals"
            )

            GitlabFindMRApprovalResponse(
                error = null,
                approval = this.json.parse(ApprovalModel.serializer(), response)
            )
        },
        failed = {
            GitlabFindMRApprovalResponse(it, ApprovalModel.Empty)
        }
    )
}