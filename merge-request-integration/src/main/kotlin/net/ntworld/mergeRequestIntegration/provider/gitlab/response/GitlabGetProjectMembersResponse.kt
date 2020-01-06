package net.ntworld.mergeRequestIntegration.provider.gitlab.response

import net.ntworld.foundation.Error
import net.ntworld.foundation.Response
import org.gitlab4j.api.models.Member

data class GitlabGetProjectMembersResponse(
    override val error: Error?,
    val members: List<Member>
): Response
