package net.ntworld.mergeRequest.query

import net.ntworld.foundation.cqrs.Query

interface GetCommentsQuery : QueryBase, Query<GetCommentsQueryResult> {
    val mergeRequestId: String

    companion object
}

