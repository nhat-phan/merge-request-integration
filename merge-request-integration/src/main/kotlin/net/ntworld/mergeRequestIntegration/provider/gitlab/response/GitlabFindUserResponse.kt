package net.ntworld.mergeRequestIntegration.provider.gitlab.response

import net.ntworld.foundation.Error
import net.ntworld.foundation.Response
import org.gitlab4j.api.models.User

data class GitlabFindUserResponse(
    override val error: Error?,
    val user: User
): Response