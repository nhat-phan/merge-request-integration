package net.ntworld.mergeRequestIntegration.provider.gitlab

import net.ntworld.foundation.Infrastructure
import net.ntworld.mergeRequest.Project
import net.ntworld.mergeRequest.UserInfo
import net.ntworld.mergeRequest.api.ApiCredentials
import net.ntworld.mergeRequest.api.ProjectApi
import net.ntworld.mergeRequestIntegration.provider.gitlab.request.GitlabFindProjectRequest
import net.ntworld.mergeRequestIntegration.provider.gitlab.request.GitlabGetProjectMembersRequest
import net.ntworld.mergeRequestIntegration.provider.gitlab.transformer.GitlabMemberTransformer
import net.ntworld.mergeRequestIntegration.provider.gitlab.transformer.GitlabProjectTransformer

class GitlabProjectApi(
    private val infrastructure: Infrastructure,
    private val credentials: ApiCredentials
) : ProjectApi {

    override fun find(projectId: String): Project? {
        val out = infrastructure.serviceBus() process GitlabFindProjectRequest(credentials, projectId.toInt())

        return if (out.hasError()) {
            null
        } else {
            GitlabProjectTransformer.transform(out.getResponse().project)
        }
    }

    override fun getMembers(projectId: String): List<UserInfo> {
        val out = infrastructure.serviceBus() process GitlabGetProjectMembersRequest(credentials)

        return if (out.hasError()) {
            listOf()
        } else {
            out.getResponse().members.map { GitlabMemberTransformer.transform(it) }
        }
    }
}