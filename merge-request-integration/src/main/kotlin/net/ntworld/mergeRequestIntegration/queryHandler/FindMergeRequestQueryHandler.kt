package net.ntworld.mergeRequestIntegration.queryHandler

import net.ntworld.foundation.Handler
import net.ntworld.foundation.cqrs.QueryHandler
import net.ntworld.mergeRequest.query.FindMergeRequestQuery
import net.ntworld.mergeRequest.query.FindMergeRequestQueryResult
import net.ntworld.mergeRequestIntegration.ProviderStorage
import net.ntworld.mergeRequestIntegration.make

@Handler
class FindMergeRequestQueryHandler(
    private val providerStorage: ProviderStorage
): QueryHandler<FindMergeRequestQuery, FindMergeRequestQueryResult> {

    override fun handle(query: FindMergeRequestQuery): FindMergeRequestQueryResult {
        val (data, api) = providerStorage.findOrFail(query.providerId)
        return FindMergeRequestQueryResult.make(
            mergeRequest = api.mergeRequest.findOrFail(data.project.id, query.mergeRequestId)
        )
    }

}