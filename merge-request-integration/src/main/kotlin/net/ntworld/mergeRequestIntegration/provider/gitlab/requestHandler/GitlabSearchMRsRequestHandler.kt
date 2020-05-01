package net.ntworld.mergeRequestIntegration.provider.gitlab.requestHandler

import net.ntworld.foundation.Handler
import net.ntworld.foundation.RequestHandler
import net.ntworld.mergeRequestIntegration.provider.gitlab.GitlabClient
import net.ntworld.mergeRequestIntegration.provider.gitlab.GitlabUtil
import net.ntworld.mergeRequestIntegration.provider.gitlab.request.GitlabSearchMRsRequest
import net.ntworld.mergeRequestIntegration.provider.gitlab.response.GitlabSearchMRsResponse
import org.gitlab4j.api.GitLabApiForm
import org.gitlab4j.api.models.MergeRequestFilter

@Handler
class GitlabSearchMRsRequestHandler : RequestHandler<GitlabSearchMRsRequest, GitlabSearchMRsResponse> {
    override fun handle(request: GitlabSearchMRsRequest): GitlabSearchMRsResponse = GitlabClient(
        request = request,
        execute = {
            val filter = buildMergeRequestFilter(request)
            val pager = this.mergeRequestApi.getMergeRequests(filter, request.perPage)
            val result = pager.page(if (request.page <= pager.totalPages) request.page else pager.totalPages)
            GitlabSearchMRsResponse(
                error = null, mergeRequests = result,
                totalPages = pager.totalPages,
                totalItems = pager.totalItems,
                currentPage = pager.currentPage
            )
        },
        failed = {
            GitlabSearchMRsResponse(
                error = it,
                mergeRequests = listOf(),
                totalPages = 0,
                totalItems = 0,
                currentPage = 0
            )
        }
    )

    internal fun buildMergeRequestFilter(request: GitlabSearchMRsRequest): MyMergeRequestFilter {
        val filter = MyMergeRequestFilter()
        if (request.sourceBranch.isNotEmpty()) {
            filter.sourceBranch = request.sourceBranch
        }
        filter.state = request.state
        filter.projectId = request.credentials.projectId.toInt()

        setSearchFilterParamIfNotEmpty(filter, request)
        setAuthorFilterParamIfNotEmpty(filter, request)
        setAssigneeFilterParamIfNotEmpty(filter, request)
        setApproverIdsParamIfNotEmpty(filter, request)

        filter.orderBy = request.orderBy
        filter.sort = request.sort
        return filter
    }

    private fun setSearchFilterParamIfNotEmpty(filter: MyMergeRequestFilter, request: GitlabSearchMRsRequest) {
        if (request.search.isNotEmpty()) {
            filter.search = request.search
        }
    }

    private fun setAuthorFilterParamIfNotEmpty(filter: MyMergeRequestFilter, request: GitlabSearchMRsRequest) {
        if (request.authorId.isNotEmpty()) {
            filter.authorId = request.authorId.toInt()
        }
    }

    private fun setAssigneeFilterParamIfNotEmpty(filter: MyMergeRequestFilter, request: GitlabSearchMRsRequest) {
        if (request.assigneeId.isNotEmpty()) {
            filter.assigneeId = request.assigneeId.toInt()
        }
    }

    private fun setApproverIdsParamIfNotEmpty(filter: MyMergeRequestFilter, request: GitlabSearchMRsRequest) {
        if (GitlabUtil.hasMergeApprovalFeature(request.credentials)) {
            val approverIds = request.approverIds
                .filter { it.isNotEmpty() }
                .map { it.toInt() }
            if (approverIds.isNotEmpty()) {
                filter.approverIds = approverIds
            }
        }
    }

    internal class MyMergeRequestFilter : MergeRequestFilter() {
        var approverIds: List<Int> = listOf()

        override fun getQueryParams(): GitLabApiForm {
            val form = super.getQueryParams()
            form.withParam("author_id", this.authorId)
            val approvers = approverIds.filter { it > 0 }
            if (approvers.isNotEmpty()) {
                form.withParam("approver_ids", approvers)
            }
            return form
        }
    }
}