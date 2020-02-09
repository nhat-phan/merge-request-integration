package net.ntworld.mergeRequestIntegration.provider.github

import net.ntworld.foundation.Infrastructure
import net.ntworld.mergeRequest.Project
import net.ntworld.mergeRequest.UserInfo
import net.ntworld.mergeRequest.api.ApiCredentials
import net.ntworld.mergeRequest.api.ProjectApi
import net.ntworld.mergeRequestIntegration.provider.github.request.GithubFindRepositoryRequest
import net.ntworld.mergeRequestIntegration.provider.github.transformer.GithubRepositoryTransformer
import net.ntworld.mergeRequestIntegration.provider.github.vo.GithubProjectId

class GithubProjectApi(
    private val infrastructure: Infrastructure,
    private val credentials: ApiCredentials
) : ProjectApi {

    override fun find(projectId: String): Project? {
        val out = infrastructure.serviceBus() process GithubFindRepositoryRequest(
            credentials, repositoryId = GithubProjectId.parseId(projectId).toString()
        )

        return if (out.hasError()) {
            null
        } else {
            GithubRepositoryTransformer.transform(out.getResponse().repository)
        }
    }

    override fun getMembers(projectId: String): List<UserInfo> {
        // TODO: Get members of project
        return listOf()
    }

}