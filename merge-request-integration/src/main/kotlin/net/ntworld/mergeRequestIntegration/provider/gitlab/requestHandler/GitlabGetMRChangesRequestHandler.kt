package net.ntworld.mergeRequestIntegration.provider.gitlab.requestHandler

import net.ntworld.foundation.Handler
import net.ntworld.foundation.RequestHandler
import net.ntworld.mergeRequestIntegration.provider.gitlab.GitlabClient
import net.ntworld.mergeRequestIntegration.provider.gitlab.request.GitlabGetMRChangesRequest
import net.ntworld.mergeRequestIntegration.provider.gitlab.response.GitlabGetMRChangesResponse

@Handler
class GitlabGetMRChangesRequestHandler : RequestHandler<GitlabGetMRChangesRequest, GitlabGetMRChangesResponse> {

    override fun handle(request: GitlabGetMRChangesRequest): GitlabGetMRChangesResponse = GitlabClient(
        request = request,
        execute = {
            val mr = this.mergeRequestApi.getMergeRequestChanges(
                request.credentials.projectId.toInt(), request.mergeRequestInternalId
            )
            GitlabGetMRChangesResponse(error = null, changes = mr.changes)
        },
        failed = {
            GitlabGetMRChangesResponse(error = it, changes = listOf())
        }
    )

}