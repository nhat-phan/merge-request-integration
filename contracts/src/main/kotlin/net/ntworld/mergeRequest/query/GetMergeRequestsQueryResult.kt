package net.ntworld.mergeRequest.query

import net.ntworld.foundation.cqrs.QueryResult
import net.ntworld.mergeRequest.MergeRequestInfo

interface GetMergeRequestsQueryResult : QueryResult {
    val mergeRequests: List<MergeRequestInfo>

    val totalPages: Int

    val totalItems: Int

    val currentPage: Int

    companion object
}