package net.ntworld.mergeRequest.query

import net.ntworld.foundation.cqrs.QueryResult
import net.ntworld.mergeRequest.Pipeline

interface GetPipelinesQueryResult : QueryResult {
    val pipelines: List<Pipeline>

    companion object
}
