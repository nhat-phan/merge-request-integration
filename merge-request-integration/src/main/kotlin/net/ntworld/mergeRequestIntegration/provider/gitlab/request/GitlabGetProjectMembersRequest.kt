package net.ntworld.mergeRequestIntegration.provider.gitlab.request

import net.ntworld.foundation.Request
import net.ntworld.mergeRequest.api.ApiCredentials
import net.ntworld.mergeRequestIntegration.provider.gitlab.GitlabRequest
import net.ntworld.mergeRequestIntegration.provider.gitlab.response.GitlabGetProjectMembersResponse

data class GitlabGetProjectMembersRequest(
    override val credentials: ApiCredentials
): GitlabRequest, Request<GitlabGetProjectMembersResponse>
