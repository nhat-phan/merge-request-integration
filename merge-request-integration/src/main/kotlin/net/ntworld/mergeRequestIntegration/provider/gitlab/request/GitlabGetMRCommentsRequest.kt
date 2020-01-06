package net.ntworld.mergeRequestIntegration.provider.gitlab.request

import net.ntworld.foundation.Request
import net.ntworld.mergeRequest.api.ApiCredentials
import net.ntworld.mergeRequestIntegration.provider.gitlab.GitlabRequest
import net.ntworld.mergeRequestIntegration.provider.gitlab.response.GitlabGetMRCommentsResponse

data class GitlabGetMRCommentsRequest(
    override val credentials: ApiCredentials,
    val projectFullPath: String,
    val endCursor: String,
    val mergeRequestInternalId: Int
) : GitlabRequest, Request<GitlabGetMRCommentsResponse>
