package net.ntworld.mergeRequestIntegration.provider.github.response

import net.ntworld.foundation.Error
import net.ntworld.foundation.Response
import org.kohsuke.github.GHRepository

data class GithubFindRepositoryResponse(
    override val error: Error?,
    val repository: GHRepository
) : Response
