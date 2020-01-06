package net.ntworld.mergeRequestIntegration.provider.gitlab.requestHandler

import net.ntworld.foundation.Handler
import net.ntworld.foundation.RequestHandler
import net.ntworld.mergeRequestIntegration.provider.gitlab.GitlabClient
import net.ntworld.mergeRequestIntegration.provider.gitlab.request.GitlabFindMRRequest
import net.ntworld.mergeRequestIntegration.provider.gitlab.response.GitlabFindMRResponse
import org.gitlab4j.api.models.MergeRequest

@Handler
class GitlabFindMRRequestHandler : RequestHandler<GitlabFindMRRequest, GitlabFindMRResponse> {
    override fun handle(request: GitlabFindMRRequest): GitlabFindMRResponse = GitlabClient(
        request = request,
        execute = {
            val mr = this.mergeRequestApi.getMergeRequest(
                request.credentials.projectId.toInt(),
                request.mergeRequestInternalId.toInt()
            )
            GitlabFindMRResponse(null, mr)
        },
        failed = {
            GitlabFindMRResponse(it, MergeRequest())
        }
    )
}