package net.ntworld.mergeRequest.query

import net.ntworld.foundation.cqrs.Query

interface GetProjectMembersQuery : QueryBase, Query<GetProjectMembersQueryResult> {
    companion object
}
