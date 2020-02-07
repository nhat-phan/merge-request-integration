package net.ntworld.mergeRequestIntegration.provider.github.request

import net.ntworld.foundation.Request
import net.ntworld.mergeRequest.api.ApiCredentials
import net.ntworld.mergeRequestIntegration.provider.github.GithubRequest
import net.ntworld.mergeRequestIntegration.provider.github.response.GithubSearchRepositoriesResponse

data class GithubSearchRepositoriesRequest(
    override val credentials: ApiCredentials,
    val term: String
) : GithubRequest, Request<GithubSearchRepositoriesResponse>