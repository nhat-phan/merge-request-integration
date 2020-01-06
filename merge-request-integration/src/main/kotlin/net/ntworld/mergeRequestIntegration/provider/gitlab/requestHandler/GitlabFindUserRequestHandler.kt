package net.ntworld.mergeRequestIntegration.provider.gitlab.requestHandler

import net.ntworld.foundation.Error
import net.ntworld.foundation.Handler
import net.ntworld.foundation.RequestHandler
import net.ntworld.mergeRequestIntegration.provider.gitlab.GitlabClient
import net.ntworld.mergeRequestIntegration.provider.gitlab.GitlabFailedRequestError
import net.ntworld.mergeRequestIntegration.provider.gitlab.request.GitlabFindUserRequest
import net.ntworld.mergeRequestIntegration.provider.gitlab.response.GitlabFindUserResponse
import org.gitlab4j.api.models.User

@Handler
class GitlabFindUserRequestHandler : RequestHandler<GitlabFindUserRequest, GitlabFindUserResponse> {
    override fun handle(request: GitlabFindUserRequest): GitlabFindUserResponse = GitlabClient(
        request = request,
        execute = {
            val user = this.userApi.getUser(request.userId.toInt())
            if (null == user) {
                GitlabFindUserResponse(
                    error = GitlabFailedRequestError("User not found", 404),
                    user = User()
                )
            } else {
                GitlabFindUserResponse(error = null, user = user)
            }
        },
        failed = {
            GitlabFindUserResponse(error = it, user = User())
        }
    )
}