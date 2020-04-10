package net.ntworld.mergeRequestIntegration.provider.gitlab

import net.ntworld.foundation.Infrastructure
import net.ntworld.mergeRequest.Change
import net.ntworld.mergeRequest.api.ApiCredentials
import net.ntworld.mergeRequest.api.CommitApi
import net.ntworld.mergeRequestIntegration.provider.gitlab.request.GitlabGetCommitChangesRequest
import net.ntworld.mergeRequestIntegration.provider.gitlab.transformer.GitlabDiffTransformer

class GitlabCommitApi(
    private val infrastructure: Infrastructure,
    private val credentials: ApiCredentials
) : CommitApi {

    override fun getChanges(projectId: String, commitId: String): List<Change> {
        val out = infrastructure.serviceBus() process GitlabGetCommitChangesRequest(
            credentials = credentials,
            commitSha = commitId
        )
        return if (out.hasError()) {
            listOf()
        } else {
            out.getResponse().changes.map { GitlabDiffTransformer.transform(it) }
        }
    }

}