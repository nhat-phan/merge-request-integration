package net.ntworld.mergeRequestIntegration.provider.gitlab.requestHandler

import net.ntworld.foundation.Handler
import net.ntworld.foundation.RequestHandler
import net.ntworld.mergeRequestIntegration.provider.gitlab.GitlabClient
import net.ntworld.mergeRequestIntegration.provider.gitlab.request.GitlabFindCurrentUserRequest
import net.ntworld.mergeRequestIntegration.provider.gitlab.response.GitlabFindUserResponse
import org.gitlab4j.api.models.User

@Handler
class GitlabFindCurrentUserRequestHandler
    : RequestHandler<GitlabFindCurrentUserRequest, GitlabFindUserResponse> {

    override fun handle(request: GitlabFindCurrentUserRequest): GitlabFindUserResponse = GitlabClient(
        request = request,
        execute = {
            GitlabFindUserResponse(error = null, user = this.userApi.currentUser)
        },
        failed = {
            GitlabFindUserResponse(error = it, user = User())
        }
    )

}