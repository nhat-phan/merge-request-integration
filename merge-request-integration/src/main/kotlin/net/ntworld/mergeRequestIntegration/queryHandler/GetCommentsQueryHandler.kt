package net.ntworld.mergeRequestIntegration.queryHandler

import net.ntworld.foundation.Handler
import net.ntworld.foundation.cqrs.QueryHandler
import net.ntworld.mergeRequest.query.GetCommentsQuery
import net.ntworld.mergeRequest.query.GetCommentsQueryResult
import net.ntworld.mergeRequestIntegration.ProviderStorage
import net.ntworld.mergeRequestIntegration.make

@Handler
class GetCommentsQueryHandler(
    private val providerStorage: ProviderStorage
) : QueryHandler<GetCommentsQuery, GetCommentsQueryResult> {

    override fun handle(query: GetCommentsQuery): GetCommentsQueryResult {
        val (data, api) = providerStorage.findOrFail(query.providerId)
        return GetCommentsQueryResult.make(
            comments = api.comment.getAll(data.project, query.mergeRequestId)
        )
    }

}