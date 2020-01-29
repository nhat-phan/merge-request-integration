package net.ntworld.mergeRequestIntegration.provider.gitlab

import net.ntworld.mergeRequest.MergeRequest
import net.ntworld.mergeRequest.api.Cache
import net.ntworld.mergeRequest.api.MergeRequestApi
import net.ntworld.mergeRequestIntegration.provider.MergeRequestApiDecorator

class GitlabMergeRequestApiCache(
    private val api: MergeRequestApi,
    private val cache: Cache
) : MergeRequestApiDecorator(api) {

    override fun findOrFail(projectId: String, mergeRequestId: String): MergeRequest {
        val key = "MR:find:$mergeRequestId"
        return cache.getOrRun(key) {
            val mergeRequest = super.findOrFail(projectId, mergeRequestId)

            cache.set(key, mergeRequest)
            mergeRequest
        }
    }

}