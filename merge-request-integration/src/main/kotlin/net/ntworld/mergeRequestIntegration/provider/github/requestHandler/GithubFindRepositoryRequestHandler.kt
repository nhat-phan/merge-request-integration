package net.ntworld.mergeRequestIntegration.provider.github.requestHandler

import net.ntworld.foundation.Handler
import net.ntworld.foundation.RequestHandler
import net.ntworld.mergeRequestIntegration.provider.github.GithubClient
import net.ntworld.mergeRequestIntegration.provider.github.GithubUtil
import net.ntworld.mergeRequestIntegration.provider.github.request.GithubFindRepositoryRequest
import net.ntworld.mergeRequestIntegration.provider.github.response.GithubFindRepositoryResponse
import org.kohsuke.github.GHRepository

@Handler
class GithubFindRepositoryRequestHandler : RequestHandler<GithubFindRepositoryRequest, GithubFindRepositoryResponse> {
    override fun handle(request: GithubFindRepositoryRequest): GithubFindRepositoryResponse = GithubClient(
        request = request,
        execute = {
            val repository = this.getRepositoryById(request.repositoryId)

            GithubFindRepositoryResponse(error = null, repository = repository)
        },
        failed = {
            GithubFindRepositoryResponse(error = it, repository = GHRepository())
        }
    )
}