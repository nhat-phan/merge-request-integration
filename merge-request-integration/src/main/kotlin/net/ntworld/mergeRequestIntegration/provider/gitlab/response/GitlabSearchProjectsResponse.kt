package net.ntworld.mergeRequestIntegration.provider.gitlab.response

import net.ntworld.foundation.Error
import net.ntworld.foundation.Response
import org.gitlab4j.api.models.Project as GitlabProject

data class GitlabSearchProjectsResponse(
    override val error: Error?,
    val projects: List<GitlabProject>
) : Response
