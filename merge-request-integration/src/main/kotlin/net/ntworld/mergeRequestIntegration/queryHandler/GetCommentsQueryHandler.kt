package net.ntworld.mergeRequestIntegration.queryHandler

import net.ntworld.foundation.Handler
import net.ntworld.foundation.cqrs.QueryHandler
import net.ntworld.mergeRequest.query.GetCommentsQuery
import net.ntworld.mergeRequest.query.GetCommentsQueryResult
import net.ntworld.mergeRequestIntegration.ApiProviderManager
import net.ntworld.mergeRequestIntegration.make

@Handler
class GetCommentsQueryHandler : QueryHandler<GetCommentsQuery, GetCommentsQueryResult> {

    override fun handle(query: GetCommentsQuery): GetCommentsQueryResult {
        val (data, api) = ApiProviderManager.findOrFail(query.providerId)
        return GetCommentsQueryResult.make(
            comments = api.comment.getAll(data.project, query.mergeRequestId)
        )
    }

}