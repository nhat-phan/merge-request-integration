package net.ntworld.mergeRequestIntegration.provider.github.response

import net.ntworld.foundation.Error
import net.ntworld.foundation.Response
import net.ntworld.mergeRequestIntegration.provider.github.model.PullRequestSearchItem
import net.ntworld.mergeRequestIntegration.provider.github.model.SearchPullRequestResult

data class GithubSearchPRsResponse(
    override val error: Error?,
    val result: SearchPullRequestResult
): Response
