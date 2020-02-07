package net.ntworld.mergeRequestIntegration.provider.github.request

import net.ntworld.foundation.Request
import net.ntworld.mergeRequest.api.ApiCredentials
import net.ntworld.mergeRequestIntegration.provider.github.GithubRequest
import net.ntworld.mergeRequestIntegration.provider.github.response.GithubFindRepositoryResponse

data class GithubFindRepositoryRequest(
    override val credentials: ApiCredentials,
    val repositoryId: String
): GithubRequest, Request<GithubFindRepositoryResponse>

