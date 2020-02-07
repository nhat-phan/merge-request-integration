package net.ntworld.mergeRequestIntegration.provider.github.requestHandler

import net.ntworld.foundation.Handler
import net.ntworld.foundation.RequestHandler
import net.ntworld.mergeRequestIntegration.provider.github.request.GithubSearchRepositoriesRequest
import net.ntworld.mergeRequestIntegration.provider.github.response.GithubSearchRepositoriesResponse
import org.kohsuke.github.GitHubBuilder

@Handler
class GithubSearchRepositoriesRequestHandler : RequestHandler<GithubSearchRepositoriesRequest, GithubSearchRepositoriesResponse> {
    override fun handle(request: GithubSearchRepositoriesRequest): GithubSearchRepositoriesResponse {
        val github = GitHubBuilder()
            .withEndpoint(request.credentials.url)
            .withPassword(
                request.credentials.login,
                request.credentials.token
            )
            .build()
        val search = github.searchRepositories().q(request.term)
        val iterable = search.list().withPageSize(10)
        val data = iterable.iterator().nextPage()
        return GithubSearchRepositoriesResponse(
            error = null,
            repositories = data
        )
    }
}