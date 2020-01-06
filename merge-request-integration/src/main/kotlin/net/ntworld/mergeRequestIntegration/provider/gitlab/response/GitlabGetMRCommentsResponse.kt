package net.ntworld.mergeRequestIntegration.provider.gitlab.response

import net.ntworld.foundation.Error
import net.ntworld.foundation.Response
import net.ntworld.mergeRequestIntegration.provider.gitlab.model.GetCommentsPayload

data class GitlabGetMRCommentsResponse(
    override val error: Error?,
    val payload: GetCommentsPayload?
) : Response
