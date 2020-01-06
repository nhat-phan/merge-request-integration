package net.ntworld.mergeRequest.query

import net.ntworld.foundation.cqrs.QueryResult
import net.ntworld.mergeRequest.Commit

interface GetCommitsQueryResult : QueryResult {
    val commits: List<Commit>

    companion object
}
