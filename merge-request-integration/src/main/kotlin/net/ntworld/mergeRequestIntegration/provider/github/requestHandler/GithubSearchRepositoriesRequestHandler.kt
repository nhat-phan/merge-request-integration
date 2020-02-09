package net.ntworld.mergeRequestIntegration.provider.github.requestHandler

import net.ntworld.foundation.Handler
import net.ntworld.foundation.RequestHandler
import net.ntworld.mergeRequestIntegration.provider.github.GithubClient
import net.ntworld.mergeRequestIntegration.provider.github.request.GithubSearchRepositoriesRequest
import net.ntworld.mergeRequestIntegration.provider.github.response.GithubSearchRepositoriesResponse
import org.kohsuke.github.GitHubBuilder

@Handler
class GithubSearchRepositoriesRequestHandler : RequestHandler<GithubSearchRepositoriesRequest, GithubSearchRepositoriesResponse> {

    override fun handle(request: GithubSearchRepositoriesRequest): GithubSearchRepositoriesResponse = GithubClient(
        request = request,
        execute = {
            val search = this.searchRepositories().q(request.term)
            val iterable = search.list().withPageSize(10)
            val data = iterable.iterator().nextPage()
            GithubSearchRepositoriesResponse(error = null, repositories = data)
        },
        failed = {
            GithubSearchRepositoriesResponse(error = it, repositories = listOf())
        }
    )

}