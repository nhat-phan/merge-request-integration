package net.ntworld.mergeRequestIntegration.provider.gitlab.request

import net.ntworld.foundation.Request
import net.ntworld.mergeRequest.api.ApiCredentials
import net.ntworld.mergeRequestIntegration.provider.gitlab.GitlabRequest
import net.ntworld.mergeRequestIntegration.provider.gitlab.response.GitlabFindProjectResponse

// FIXME: projectId, mergeRequestInternalId in this package belong to Gitlab, then it should be an integer not String
data class GitlabFindProjectRequest(
    override val credentials: ApiCredentials,
    val projectId: String
) : GitlabRequest, Request<GitlabFindProjectResponse>
