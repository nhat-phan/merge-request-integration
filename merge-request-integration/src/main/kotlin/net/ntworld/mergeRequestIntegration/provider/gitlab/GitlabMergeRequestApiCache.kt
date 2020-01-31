package net.ntworld.mergeRequestIntegration.provider.gitlab

import net.ntworld.mergeRequest.MergeRequest
import net.ntworld.mergeRequest.api.*
import net.ntworld.mergeRequest.query.GetMergeRequestFilter
import net.ntworld.mergeRequestIntegration.internal.ApiOptionsImpl
import net.ntworld.mergeRequestIntegration.provider.MergeRequestApiDecorator
import org.joda.time.DateTime

class GitlabMergeRequestApiCache(
    private val api: MergeRequestApi,
    private val cache: Cache
) : MergeRequestApiDecorator(api) {
    var options: ApiOptions = ApiOptionsImpl.DEFAULT

    override fun findOrFail(projectId: String, mergeRequestId: String): MergeRequest {
        if (!options.enableRequestCache) {
            return super.findOrFail(projectId, mergeRequestId)
        }

        val key = makeFindCacheKey(mergeRequestId)
        return cache.getOrRun(key) {
            val mergeRequest = super.findOrFail(projectId, mergeRequestId)

            cache.set(key, mergeRequest)
            mergeRequest
        }
    }

    override fun search(
        projectId: String,
        currentUserId: String,
        filterBy: GetMergeRequestFilter,
        orderBy: MergeRequestOrdering,
        page: Int,
        itemsPerPage: Int
    ): MergeRequestApi.SearchResult {
        val result = super.search(projectId, currentUserId, filterBy, orderBy, page, itemsPerPage)
        if (options.enableRequestCache) {
            result.data.forEach {
                try {
                    val key = makeFindCacheKey(it.id)
                    if (cache.isExpiredAfter(key, DateTime(it.updatedAt))) {
                        cache.remove(key)
                    }
                } catch (cacheNotFound: CacheNotFoundException) {
                }
            }
        }
        return result
    }

    private fun makeFindCacheKey(mergeRequestId: String) = "MR:find:$mergeRequestId"
}