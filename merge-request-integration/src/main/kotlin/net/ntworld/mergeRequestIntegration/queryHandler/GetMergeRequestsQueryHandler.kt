package net.ntworld.mergeRequestIntegration.queryHandler

import net.ntworld.foundation.Handler
import net.ntworld.foundation.cqrs.QueryHandler
import net.ntworld.mergeRequest.query.GetMergeRequestsQuery
import net.ntworld.mergeRequest.query.GetMergeRequestsQueryResult
import net.ntworld.mergeRequestIntegration.ApiProviderManager
import net.ntworld.mergeRequestIntegration.make

@Handler
class GetMergeRequestsQueryHandler : QueryHandler<GetMergeRequestsQuery, GetMergeRequestsQueryResult> {

    override fun handle(query: GetMergeRequestsQuery): GetMergeRequestsQueryResult {
        val (data, api) = ApiProviderManager.findOrFail(query.providerId)
        val result = api.mergeRequest.search(
            projectId = data.project.id,
            currentUserId = data.currentUser.id,
            filterBy = query.filterBy,
            orderBy = query.orderBy,
            page = query.page,
            itemsPerPage = query.itemsPerPage
        )
        return GetMergeRequestsQueryResult.make(
            mergeRequests = result.data,
            totalPages = result.totalPages,
            totalItems = result.totalItems,
            currentPage = result.currentPage
        )
    }

}
