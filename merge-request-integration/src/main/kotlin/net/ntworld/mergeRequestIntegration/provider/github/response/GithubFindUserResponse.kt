package net.ntworld.mergeRequestIntegration.provider.github.response

import net.ntworld.foundation.Error
import net.ntworld.foundation.Response
import org.kohsuke.github.GHUser

data class GithubFindUserResponse(
    override val error: Error?,
    val user: GHUser
): Response