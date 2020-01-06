package net.ntworld.mergeRequestIntegration.provider.gitlab.requestHandler

import net.ntworld.foundation.Handler
import net.ntworld.foundation.RequestHandler
import net.ntworld.mergeRequestIntegration.provider.gitlab.GitlabClient
import net.ntworld.mergeRequestIntegration.provider.gitlab.request.GitlabGetMRDiscussionsRequest
import net.ntworld.mergeRequestIntegration.provider.gitlab.response.GitlabGetMRDiscussionsResponse

@Handler
class GitlabGetMRDiscussionsRequestHandler
    : RequestHandler<GitlabGetMRDiscussionsRequest, GitlabGetMRDiscussionsResponse> {

    override fun handle(request: GitlabGetMRDiscussionsRequest): GitlabGetMRDiscussionsResponse = GitlabClient(
        request = request,
        execute = {
            val discussions = this.discussionsApi.getMergeRequestDiscussions(
                request.credentials.projectId.toInt(),
                request.mergeRequestInternalId
            )
            GitlabGetMRDiscussionsResponse(error = null, discussions = discussions)
        },
        failed = {
            GitlabGetMRDiscussionsResponse(error = it, discussions = listOf())
        }
    )

}