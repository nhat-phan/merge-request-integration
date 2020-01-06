package net.ntworld.mergeRequest.query

import net.ntworld.foundation.cqrs.QueryResult
import net.ntworld.mergeRequest.Approval

interface FindApprovalQueryResult : QueryResult {
    val approval: Approval

    companion object
}