package net.ntworld.mergeRequestIntegration.provider.gitlab.request

import net.ntworld.foundation.Request
import net.ntworld.mergeRequest.api.ApiCredentials
import net.ntworld.mergeRequestIntegration.provider.gitlab.GitlabRequest
import net.ntworld.mergeRequestIntegration.provider.gitlab.response.GitlabGetCommitChangesResponse

data class GitlabGetCommitChangesRequest(
    override val credentials: ApiCredentials,
    val commitSha: String
): GitlabRequest, Request<GitlabGetCommitChangesResponse>