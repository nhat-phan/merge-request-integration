package net.ntworld.mergeRequest.query

import net.ntworld.foundation.cqrs.Query
import net.ntworld.mergeRequest.api.MergeRequestOrdering

interface GetMergeRequestsQuery : QueryBase, Query<GetMergeRequestsQueryResult> {
    val filterBy: GetMergeRequestFilter

    val orderBy: MergeRequestOrdering

    val page: Int

    val itemsPerPage: Int

    companion object
}

