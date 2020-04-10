package net.ntworld.mergeRequestIntegration.provider.gitlab.requestHandler

import net.ntworld.foundation.Handler
import net.ntworld.foundation.RequestHandler
import net.ntworld.mergeRequestIntegration.provider.gitlab.GitlabClient
import net.ntworld.mergeRequestIntegration.provider.gitlab.request.GitlabGetCommitChangesRequest
import net.ntworld.mergeRequestIntegration.provider.gitlab.response.GitlabGetCommitChangesResponse

@Handler
class GitlabGetCommitChangesRequestHandler : RequestHandler<GitlabGetCommitChangesRequest, GitlabGetCommitChangesResponse> {
    override fun handle(request: GitlabGetCommitChangesRequest): GitlabGetCommitChangesResponse = GitlabClient(
        request = request,
        execute = {
            val diff = this.commitsApi.getDiff(
                request.credentials.projectId.toInt(), request.commitSha
            )
            GitlabGetCommitChangesResponse(error = null, changes = diff)
        },
        failed = {
            GitlabGetCommitChangesResponse(error = it, changes = listOf())
        }
    )
}