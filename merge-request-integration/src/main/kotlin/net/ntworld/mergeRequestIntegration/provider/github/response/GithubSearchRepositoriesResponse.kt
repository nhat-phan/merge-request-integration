package net.ntworld.mergeRequestIntegration.provider.github.response

import net.ntworld.foundation.Error
import net.ntworld.foundation.Response
import org.kohsuke.github.GHRepository

data class GithubSearchRepositoriesResponse(
    override val error: Error?,
    val repositories: List<GHRepository>
): Response