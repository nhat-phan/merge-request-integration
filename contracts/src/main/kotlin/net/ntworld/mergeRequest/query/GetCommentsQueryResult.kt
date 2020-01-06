package net.ntworld.mergeRequest.query

import net.ntworld.foundation.cqrs.QueryResult
import net.ntworld.mergeRequest.Comment

interface GetCommentsQueryResult : QueryResult {
    val comments: List<Comment>

    companion object
}