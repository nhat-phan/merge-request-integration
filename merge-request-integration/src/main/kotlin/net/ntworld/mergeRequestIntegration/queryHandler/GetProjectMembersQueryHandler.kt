package net.ntworld.mergeRequestIntegration.queryHandler

import net.ntworld.foundation.Handler
import net.ntworld.foundation.cqrs.QueryHandler
import net.ntworld.mergeRequest.query.GetProjectMembersQuery
import net.ntworld.mergeRequest.query.GetProjectMembersQueryResult
import net.ntworld.mergeRequestIntegration.ApiProviderManager
import net.ntworld.mergeRequestIntegration.make

@Handler
class GetProjectMembersQueryHandler : QueryHandler<GetProjectMembersQuery, GetProjectMembersQueryResult> {

    override fun handle(query: GetProjectMembersQuery): GetProjectMembersQueryResult {
        val (data, api) = ApiProviderManager.findOrFail(query.providerId)
        return GetProjectMembersQueryResult.make(api.project.getMembers(data.project.id))
    }

}