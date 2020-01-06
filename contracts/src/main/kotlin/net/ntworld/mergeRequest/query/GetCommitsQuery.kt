package net.ntworld.mergeRequest.query

import net.ntworld.foundation.cqrs.Query

interface GetCommitsQuery : QueryBase,
    Query<GetCommitsQueryResult> {
    val mergeRequestId: String

    companion object
}
