package net.ntworld.mergeRequestIntegration.provider.github

import net.ntworld.foundation.Infrastructure
import net.ntworld.mergeRequest.Approval
import net.ntworld.mergeRequest.Commit
import net.ntworld.mergeRequest.MergeRequest
import net.ntworld.mergeRequest.Pipeline
import net.ntworld.mergeRequest.api.ApiCredentials
import net.ntworld.mergeRequest.api.MergeRequestApi
import net.ntworld.mergeRequest.api.MergeRequestOrdering
import net.ntworld.mergeRequest.query.GetMergeRequestFilter

class GithubMergeRequestApi(
    private val infrastructure: Infrastructure,
    private val credentials: ApiCredentials
) : MergeRequestApi {

    override fun find(projectId: String, mergeRequestId: String): MergeRequest? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun approve(projectId: String, mergeRequestId: String, sha: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun unapprove(projectId: String, mergeRequestId: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun findApproval(projectId: String, mergeRequestId: String): Approval {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getPipelines(projectId: String, mergeRequestId: String): List<Pipeline> {
        // TODO: get pipelines
        return listOf()
    }

    override fun getCommits(projectId: String, mergeRequestId: String): List<Commit> {
        // TODO: get commits
        return listOf()
    }

    override fun search(
        projectId: String,
        currentUserId: String,
        filterBy: GetMergeRequestFilter,
        orderBy: MergeRequestOrdering,
        page: Int,
        itemsPerPage: Int
    ): MergeRequestApi.SearchResult {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun findOrFail(projectId: String, mergeRequestId: String): MergeRequest {
        val mergeRequest = find(projectId, mergeRequestId)
        if (null === mergeRequest) {
            throw Exception("MergeRequest $mergeRequestId not found.")
        }
        return mergeRequest
    }

}