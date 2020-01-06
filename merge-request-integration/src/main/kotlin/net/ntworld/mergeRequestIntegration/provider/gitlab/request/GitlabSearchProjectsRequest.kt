package net.ntworld.mergeRequestIntegration.provider.gitlab.request

import net.ntworld.foundation.Request
import net.ntworld.mergeRequest.api.ApiCredentials
import net.ntworld.mergeRequestIntegration.provider.gitlab.GitlabRequest
import net.ntworld.mergeRequestIntegration.provider.gitlab.response.GitlabSearchProjectsResponse

data class GitlabSearchProjectsRequest(
    override val credentials: ApiCredentials,
    val term: String,
    val owner: Boolean = false,
    val starred: Boolean = false,
    val membership: Boolean = false
) : GitlabRequest, Request<GitlabSearchProjectsResponse>

