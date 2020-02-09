package net.ntworld.mergeRequestIntegration.provider.github.request

import net.ntworld.foundation.Request
import net.ntworld.mergeRequest.api.ApiCredentials
import net.ntworld.mergeRequestIntegration.provider.github.GithubRequest
import net.ntworld.mergeRequestIntegration.provider.github.response.GithubSearchPRsResponse

data class GithubSearchPRsRequest(
    override val credentials: ApiCredentials,
    val repo: String,
    val state: String,
    val author: String,
    val reviewer: String,
    val assignee: String,
    val term: String,
    val sort: String,
    val order: String,
    val page: Int = 0,
    val perPage: Int = 10
) : GithubRequest, Request<GithubSearchPRsResponse>
