package net.ntworld.mergeRequestIntegration.provider.github

import net.ntworld.foundation.Infrastructure
import net.ntworld.mergeRequest.*
import net.ntworld.mergeRequest.api.ApiCredentials
import net.ntworld.mergeRequest.api.MergeRequestApi
import net.ntworld.mergeRequest.api.MergeRequestOrdering
import net.ntworld.mergeRequest.query.GetMergeRequestFilter
import net.ntworld.mergeRequestIntegration.internal.MergeRequestSearchResultImpl
import net.ntworld.mergeRequestIntegration.provider.github.request.GithubSearchPRsRequest
import net.ntworld.mergeRequestIntegration.provider.github.transformer.GithubSearchPullRequestItemTransformer
import net.ntworld.mergeRequestIntegration.provider.github.vo.GithubProjectId
import net.ntworld.mergeRequestIntegration.provider.github.vo.GithubUserId
import kotlin.math.ceil

class GithubMergeRequestApi(
    private val infrastructure: Infrastructure,
    private val credentials: ApiCredentials
) : MergeRequestApi {

    override fun find(projectId: String, mergeRequestId: String): MergeRequest? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun approve(projectId: String, mergeRequestId: String, sha: String) {
    }

    override fun unapprove(projectId: String, mergeRequestId: String) {
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

    override fun getChanges(projectId: String, mergeRequestId: String): List<Change> {
        TODO("Not yet implemented")
    }

    override fun search(
        projectId: String,
        currentUserId: String,
        filterBy: GetMergeRequestFilter,
        orderBy: MergeRequestOrdering,
        page: Int,
        itemsPerPage: Int
    ): MergeRequestApi.SearchResult {
        val (sort, order) = resolveOrderAndSort(orderBy)
        val reviewer = if (filterBy.approverIds.isNotEmpty() && filterBy.approverIds[0].isNotEmpty()) {
            GithubUserId.parseLogin(filterBy.approverIds[0])
        } else ""
        val out = infrastructure.serviceBus() process GithubSearchPRsRequest(
            credentials = credentials,
            repo = GithubProjectId.parseFullName(credentials.projectId),
            state = when (filterBy.state) {
                MergeRequestState.ALL -> ""
                MergeRequestState.OPENED -> PULL_REQUEST_STATE_OPEN
                MergeRequestState.CLOSED -> PULL_REQUEST_STATE_CLOSED
                MergeRequestState.MERGED -> PULL_REQUEST_STATE_MERGED
            },
            author = if (filterBy.authorId.isNotEmpty()) GithubUserId.parseLogin(filterBy.authorId) else "",
            assignee = if (filterBy.assigneeId.isNotEmpty()) GithubUserId.parseLogin(filterBy.assigneeId) else "",
            reviewer = reviewer,
            term = filterBy.search,
            sort = sort,
            order = order,
            page = page,
            perPage = itemsPerPage
        )

        return if (out.hasError()) {
            MergeRequestSearchResultImpl(listOf(), 0, 0, 0)
        } else {
            val response = out.getResponse()
            val transformer = GithubSearchPullRequestItemTransformer(projectId)
            MergeRequestSearchResultImpl(
                data = response.result.items.map { transformer.transform(it) },
                totalItems = response.result.totalCount,
                totalPages = ceil(response.result.totalCount.toDouble() / itemsPerPage).toInt(),
                currentPage = page
            )
        }
    }

    private fun resolveState(state: MergeRequestState): String = when(state) {
        MergeRequestState.ALL -> "all"
        MergeRequestState.OPENED -> "open"
        MergeRequestState.CLOSED -> "closed"
        MergeRequestState.MERGED -> "merged"
    }

    private fun resolveOrderAndSort(orderBy: MergeRequestOrdering): Pair<String, String> {
        return when (orderBy) {
            MergeRequestOrdering.RECENTLY_UPDATED -> Pair("updated", "desc")
            MergeRequestOrdering.NEWEST -> Pair("created", "desc")
            MergeRequestOrdering.OLDEST -> Pair("created", "asc")
        }
    }

    override fun findOrFail(projectId: String, mergeRequestId: String): MergeRequest {
        val mergeRequest = find(projectId, mergeRequestId)
        if (null === mergeRequest) {
            throw Exception("MergeRequest $mergeRequestId not found.")
        }
        return mergeRequest
    }

}