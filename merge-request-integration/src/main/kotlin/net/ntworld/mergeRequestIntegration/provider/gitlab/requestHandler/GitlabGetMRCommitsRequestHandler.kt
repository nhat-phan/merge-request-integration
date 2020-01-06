package net.ntworld.mergeRequestIntegration.provider.gitlab.requestHandler

import net.ntworld.foundation.Handler
import net.ntworld.foundation.RequestHandler
import net.ntworld.mergeRequestIntegration.provider.gitlab.GitlabClient
import net.ntworld.mergeRequestIntegration.provider.gitlab.request.GitlabGetMRCommitsRequest
import net.ntworld.mergeRequestIntegration.provider.gitlab.response.GitlabGetMRCommitsResponse

@Handler
class GitlabGetMRCommitsRequestHandler : RequestHandler<GitlabGetMRCommitsRequest, GitlabGetMRCommitsResponse> {

    override fun handle(request: GitlabGetMRCommitsRequest): GitlabGetMRCommitsResponse = GitlabClient(
        request = request,
        execute = {
            val commits = this.mergeRequestApi.getCommits(
                request.credentials.projectId.toInt(), request.mergeRequestInternalId
            )
            GitlabGetMRCommitsResponse(error = null, commits = commits)
        },
        failed = {
            GitlabGetMRCommitsResponse(error = it, commits = listOf())
        }
    )

}