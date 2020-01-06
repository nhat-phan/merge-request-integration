package net.ntworld.mergeRequest.query

import net.ntworld.foundation.cqrs.Query

interface FindMergeRequestQuery: QueryBase, Query<FindMergeRequestQueryResult> {
    val mergeRequestId: String

    companion object
}
