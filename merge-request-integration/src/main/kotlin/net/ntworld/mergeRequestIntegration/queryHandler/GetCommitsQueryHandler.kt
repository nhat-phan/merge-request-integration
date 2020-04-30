package net.ntworld.mergeRequestIntegration.queryHandler

import net.ntworld.foundation.Handler
import net.ntworld.foundation.cqrs.QueryHandler
import net.ntworld.mergeRequest.query.GetCommitsQuery
import net.ntworld.mergeRequest.query.GetCommitsQueryResult
import net.ntworld.mergeRequestIntegration.ProviderStorage
import net.ntworld.mergeRequestIntegration.make

@Handler
class GetCommitsQueryHandler(
    private val providerStorage: ProviderStorage
) : QueryHandler<GetCommitsQuery, GetCommitsQueryResult> {

    override fun handle(query: GetCommitsQuery): GetCommitsQueryResult {
        val (data, api) = providerStorage.findOrFail(query.providerId)
        return GetCommitsQueryResult.make(
            commits = api.mergeRequest.getCommits(data.project.id, query.mergeRequestId)
        )
    }

}
