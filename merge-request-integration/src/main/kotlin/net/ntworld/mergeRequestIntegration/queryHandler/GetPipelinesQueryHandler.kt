package net.ntworld.mergeRequestIntegration.queryHandler

import net.ntworld.foundation.Handler
import net.ntworld.foundation.cqrs.QueryHandler
import net.ntworld.mergeRequest.query.GetPipelinesQuery
import net.ntworld.mergeRequest.query.GetPipelinesQueryResult
import net.ntworld.mergeRequestIntegration.ApiProviderManager
import net.ntworld.mergeRequestIntegration.make

@Handler
class GetPipelinesQueryHandler : QueryHandler<GetPipelinesQuery, GetPipelinesQueryResult> {

    override fun handle(query: GetPipelinesQuery): GetPipelinesQueryResult {
        val (data, api) = ApiProviderManager.findOrFail(query.providerId)
        return GetPipelinesQueryResult.make(
            pipelines = api.mergeRequest.getPipelines(data.project.id, query.mergeRequestId)
        )
    }

}