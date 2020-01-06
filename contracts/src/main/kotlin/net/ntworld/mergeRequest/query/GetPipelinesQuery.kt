package net.ntworld.mergeRequest.query

import net.ntworld.foundation.cqrs.Query

interface GetPipelinesQuery : QueryBase,
    Query<GetPipelinesQueryResult> {
    val mergeRequestId: String

    companion object
}