package net.ntworld.mergeRequestIntegration.provider.github.requestHandler

import net.ntworld.foundation.Handler
import net.ntworld.foundation.RequestHandler
import net.ntworld.mergeRequestIntegration.provider.github.GithubClient
import net.ntworld.mergeRequestIntegration.provider.github.request.GithubFindCurrentUserRequest
import net.ntworld.mergeRequestIntegration.provider.github.response.GithubFindUserResponse
import org.kohsuke.github.GHUser

@Handler
class GithubFindCurrentUserRequestHandler : RequestHandler<GithubFindCurrentUserRequest, GithubFindUserResponse> {
    override fun handle(request: GithubFindCurrentUserRequest): GithubFindUserResponse = GithubClient(
        request = request,
        execute = {
            GithubFindUserResponse(error = null, user = myself)
        },
        failed = {
            GithubFindUserResponse(error = it, user = GHUser())
        }
    )
}
