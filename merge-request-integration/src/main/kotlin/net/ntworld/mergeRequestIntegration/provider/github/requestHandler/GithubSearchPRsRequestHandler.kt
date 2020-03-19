package net.ntworld.mergeRequestIntegration.provider.github.requestHandler

import kotlinx.serialization.serializer
import net.ntworld.foundation.Handler
import net.ntworld.foundation.RequestHandler
import net.ntworld.mergeRequestIntegration.provider.github.GithubFuelClient
import net.ntworld.mergeRequestIntegration.provider.github.model.SearchPullRequestResult
import net.ntworld.mergeRequestIntegration.provider.github.request.GithubSearchPRsRequest
import net.ntworld.mergeRequestIntegration.provider.github.response.GithubSearchPRsResponse

@Handler
class GithubSearchPRsRequestHandler : RequestHandler<GithubSearchPRsRequest, GithubSearchPRsResponse> {

    /**
     * Because there is no way to do pagination manually with the library https://github.com/github-api/github-api/
     * then I have to change the contract & use a custom client.
     */
    override fun handle(request: GithubSearchPRsRequest): GithubSearchPRsResponse = GithubFuelClient(
        request = request,
        execute = {
            val q = mutableListOf<String>()
            q.add("repo:${request.repo}")
            q.add("is:pr")
            q.add("state:${request.state}")
            if (request.author.isNotEmpty()) {
                q.add("author:${request.author}")
            }
            if (request.reviewer.isNotEmpty()) {
                q.add("reviewed-by:${request.reviewer}")
            }
            if (request.assignee.isNotEmpty()) {
                q.add("assignee:${request.assignee}")
            }
            if (request.term.trim().isNotEmpty()) {
                q.add(request.term.trim())
            }

            val params: MutableList<Pair<String, Any?>> = mutableListOf(
                Pair("q", q.joinToString(" ")),
                Pair("sort", request.sort),
                Pair("order", request.order),
                Pair("page", request.page),
                Pair("per_page", request.perPage)
            )

            val input = this.getJson(searchIssuesUrl, params)
            GithubSearchPRsResponse(error = null, result = json.parse(
                SearchPullRequestResult.serializer(),
                input
            ))
        },
        failed = {
            GithubSearchPRsResponse(error = it, result = SearchPullRequestResult(
                totalCount = 0,
                incompleteResults = false,
                items = listOf()
            ))
        }
    )
}
