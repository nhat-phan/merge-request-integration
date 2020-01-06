package net.ntworld.mergeRequestIntegration.queryHandler

import net.ntworld.foundation.Handler
import net.ntworld.foundation.cqrs.QueryHandler
import net.ntworld.mergeRequest.query.FindApprovalQuery
import net.ntworld.mergeRequest.query.FindApprovalQueryResult
import net.ntworld.mergeRequestIntegration.ApiProviderManager
import net.ntworld.mergeRequestIntegration.make

@Handler
class FindApprovalQueryHandler : QueryHandler<FindApprovalQuery, FindApprovalQueryResult> {
    override fun handle(query: FindApprovalQuery): FindApprovalQueryResult {
        val (data, api) = ApiProviderManager.findOrFail(query.providerId)
        return FindApprovalQueryResult.make(
            approval = api.mergeRequest.findApproval(data.project.id, query.mergeRequestId)
        )
    }
}