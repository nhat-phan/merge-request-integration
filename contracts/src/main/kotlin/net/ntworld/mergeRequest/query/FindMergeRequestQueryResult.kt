package net.ntworld.mergeRequest.query

import net.ntworld.foundation.cqrs.QueryResult
import net.ntworld.mergeRequest.MergeRequest

interface FindMergeRequestQueryResult : QueryResult {
    val mergeRequest: MergeRequest

    companion object
}