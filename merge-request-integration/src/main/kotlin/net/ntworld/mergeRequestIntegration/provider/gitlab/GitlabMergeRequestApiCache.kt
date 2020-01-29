package net.ntworld.mergeRequestIntegration.provider.gitlab

import net.ntworld.mergeRequest.MergeRequest
import net.ntworld.mergeRequest.api.Cache
import net.ntworld.mergeRequest.api.CacheNotFoundException
import net.ntworld.mergeRequest.api.MergeRequestApi
import net.ntworld.mergeRequest.api.MergeRequestOrdering
import net.ntworld.mergeRequest.query.GetMergeRequestFilter
import net.ntworld.mergeRequestIntegration.provider.MergeRequestApiDecorator
import org.joda.time.DateTime

class GitlabMergeRequestApiCache(
    private val api: MergeRequestApi,
    private val cache: Cache
) : MergeRequestApiDecorator(api) {

    override fun findOrFail(projectId: String, mergeRequestId: String): MergeRequest {
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
        result.data.forEach {
            try {
                val key = makeFindCacheKey(it.id)
                if (cache.isExpiredAfter(key, DateTime(it.updatedAt))) {
                    cache.remove(key)
                }
            } catch (cacheNotFound: CacheNotFoundException) {
            }
        }
        return result
    }

    private fun makeFindCacheKey(mergeRequestId: String) = "MR:find:$mergeRequestId"
}