package net.ntworld.mergeRequestIntegration.provider.gitlab.response

import net.ntworld.foundation.Error
import net.ntworld.foundation.Response
import org.gitlab4j.api.models.Discussion

data class GitlabGetMRDiscussionsResponse(
    override val error: Error?,
    val discussions: List<Discussion>
) : Response
