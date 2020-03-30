package net.ntworld.mergeRequestIntegration.provider.gitlab.response

import net.ntworld.foundation.Error
import net.ntworld.foundation.Response
import org.gitlab4j.api.models.Diff

data class GitlabGetMRChangesResponse(
    override val error: Error?,
    val changes: List<Diff>
) : Response
