package net.ntworld.mergeRequestIntegration.provider.gitlab.requestHandler

import net.ntworld.foundation.Handler
import net.ntworld.foundation.RequestHandler
import net.ntworld.mergeRequestIntegration.provider.gitlab.GitlabClient
import net.ntworld.mergeRequestIntegration.provider.gitlab.request.GitlabGetProjectMembersRequest
import net.ntworld.mergeRequestIntegration.provider.gitlab.response.GitlabGetProjectMembersResponse

@Handler
class GitlabGetProjectMembersRequestHandler :
    RequestHandler<GitlabGetProjectMembersRequest, GitlabGetProjectMembersResponse> {

    override fun handle(request: GitlabGetProjectMembersRequest): GitlabGetProjectMembersResponse = GitlabClient(
        request = request,
        execute = {
            val members = this.projectApi.getAllMembers(request.credentials.projectId.toInt())
            GitlabGetProjectMembersResponse(error = null, members = members)
        },
        failed = {
            GitlabGetProjectMembersResponse(error = it, members = listOf())
        }
    )

}