package net.ntworld.mergeRequestIntegration.provider.gitlab

import net.ntworld.foundation.Infrastructure
import net.ntworld.mergeRequest.*
import net.ntworld.mergeRequest.api.ApiCredentials
import net.ntworld.mergeRequest.api.MergeRequestApi
import net.ntworld.mergeRequest.api.MergeRequestOrdering
import net.ntworld.mergeRequest.query.GetMergeRequestFilter
import net.ntworld.mergeRequestIntegration.provider.gitlab.command.GitlabApproveMRCommand
import net.ntworld.mergeRequestIntegration.provider.gitlab.command.GitlabUnapproveMRCommand
import net.ntworld.mergeRequestIntegration.provider.gitlab.request.*
import net.ntworld.mergeRequestIntegration.provider.gitlab.transformer.*
import org.gitlab4j.api.Constants

class GitlabMergeRequestApi(
    private val infrastructure: Infrastructure,
    private val credentials: ApiCredentials
) : MergeRequestApi {
    override fun find(projectId: String, mergeRequestId: String): MergeRequest? {
        val out = infrastructure.serviceBus() process GitlabFindMRRequest(
            credentials = credentials,
            mergeRequestInternalId = mergeRequestId
        )
        return if (out.hasError()) {
            null
        } else {
            GitlabMRTransformer.transform(out.getResponse().mergeRequest)
        }
    }

    override fun approve(projectId: String, mergeRequestId: String, sha: String) {
        infrastructure.commandBus() process GitlabApproveMRCommand(
            credentials = credentials,
            mergeRequestInternalId = mergeRequestId.toInt(),
            sha = sha
        )
    }

    override fun unapprove(projectId: String, mergeRequestId: String) {
        infrastructure.commandBus() process GitlabUnapproveMRCommand(
            credentials = credentials,
            mergeRequestInternalId = mergeRequestId.toInt()
        )
    }

    override fun findApproval(projectId: String, mergeRequestId: String): Approval {
        val out = infrastructure.serviceBus() process GitlabFindMRApprovalRequest(
            credentials = credentials,
            mergeRequestInternalId = mergeRequestId.toInt()
        )
        return GitlabApprovalTransformer.transform(out.getResponse().approval)
    }

    override fun getPipelines(projectId: String, mergeRequestId: String): List<Pipeline> {
        val out = infrastructure.serviceBus() process GitlabGetMRPipelinesRequest(
            credentials = credentials,
            mergeRequestInternalId = mergeRequestId.toInt()
        )
        return if (out.hasError()) {
            listOf()
        } else {
            out.getResponse().pipelines.map { GitlabPipelineTransformer.transform(it) }
        }
    }

    override fun getCommits(projectId: String, mergeRequestId: String): List<Commit> {
        val out = infrastructure.serviceBus() process GitlabGetMRCommitsRequest(
            credentials = credentials,
            mergeRequestInternalId = mergeRequestId.toInt()
        )
        return if (out.hasError()) {
            listOf()
        } else {
            out.getResponse().commits.map { GitlabCommitTransformer.transform(it) }
        }
    }

    override fun search(
        projectId: String,
        currentUserId: String,
        filterBy: GetMergeRequestFilter,
        orderBy: MergeRequestOrdering,
        page: Int,
        itemsPerPage: Int
    ): MergeRequestApi.SearchResult {
        val (order, sort) = resolveOrderAndSort(orderBy)
        val out = infrastructure.serviceBus() process GitlabSearchMRsRequest(
            credentials = credentials,
            state = resolveState(filterBy.state),
            search = filterBy.search,
            authorId = filterBy.authorId,
            assigneeId = filterBy.assigneeId,
            approverIds = filterBy.approverIds,
            orderBy = order,
            sort = sort,
            page = page,
            perPage = itemsPerPage
        )

        return if (out.hasError()) {
            MySearchResult(listOf(), 0, 0, 0)
        } else {
            val response = out.getResponse()
            MySearchResult(
                data = response.mergeRequests.map { GitlabMRSimpleTransformer.transform(it) },
                totalItems = response.totalItems,
                totalPages = response.totalPages,
                currentPage = response.currentPage
            )
        }
    }

    override fun findOrFail(projectId: String, mergeRequestId: String): MergeRequest  {
        val mergeRequest = find(projectId, mergeRequestId)
        if (null === mergeRequest) {
            throw Exception("MergeRequest $mergeRequestId not found.")
        }
        return mergeRequest
    }

    private fun resolveState(state: MergeRequestState): Constants.MergeRequestState = when (state) {
        MergeRequestState.ALL -> Constants.MergeRequestState.ALL
        MergeRequestState.OPENED -> Constants.MergeRequestState.OPENED
        MergeRequestState.CLOSED -> Constants.MergeRequestState.CLOSED
        MergeRequestState.MERGED -> Constants.MergeRequestState.MERGED
    }

    private fun resolveOrderAndSort(
        orderBy: MergeRequestOrdering
    ): Pair<Constants.MergeRequestOrderBy, Constants.SortOrder> {
        return when (orderBy) {
            MergeRequestOrdering.RECENTLY_UPDATED -> Pair(
                Constants.MergeRequestOrderBy.UPDATED_AT,
                Constants.SortOrder.DESC
            )
            MergeRequestOrdering.NEWEST -> Pair(Constants.MergeRequestOrderBy.CREATED_AT, Constants.SortOrder.DESC)
            MergeRequestOrdering.OLDEST -> Pair(Constants.MergeRequestOrderBy.CREATED_AT, Constants.SortOrder.ASC)
        }
    }

    private data class MySearchResult(
        override val data: List<MergeRequestInfo>,
        override val totalPages: Int,
        override val totalItems: Int,
        override val currentPage: Int
    ) : MergeRequestApi.SearchResult
}