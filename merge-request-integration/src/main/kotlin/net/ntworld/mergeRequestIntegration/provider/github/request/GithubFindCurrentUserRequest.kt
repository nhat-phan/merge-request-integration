package net.ntworld.mergeRequestIntegration.provider.github.request

import net.ntworld.foundation.Request
import net.ntworld.mergeRequest.api.ApiCredentials
import net.ntworld.mergeRequestIntegration.provider.github.GithubRequest
import net.ntworld.mergeRequestIntegration.provider.github.response.GithubFindUserResponse

data class GithubFindCurrentUserRequest(
    override val credentials: ApiCredentials
): GithubRequest, Request<GithubFindUserResponse>