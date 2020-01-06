package net.ntworld.mergeRequest.query

import net.ntworld.foundation.cqrs.QueryResult
import net.ntworld.mergeRequest.UserInfo

interface GetProjectMembersQueryResult : QueryResult {
    val members: List<UserInfo>

    companion object
}