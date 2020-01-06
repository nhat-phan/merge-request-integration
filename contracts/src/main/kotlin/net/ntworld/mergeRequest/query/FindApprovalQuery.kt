package net.ntworld.mergeRequest.query

import net.ntworld.foundation.cqrs.Query

interface FindApprovalQuery : QueryBase, Query<FindApprovalQueryResult> {
    val mergeRequestId: String

    companion object
}
