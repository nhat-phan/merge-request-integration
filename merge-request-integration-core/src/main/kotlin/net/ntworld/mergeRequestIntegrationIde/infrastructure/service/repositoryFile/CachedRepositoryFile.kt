package net.ntworld.mergeRequestIntegrationIde.infrastructure.service.repositoryFile

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.vcs.changes.Change
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequest.api.Cache
import net.ntworld.mergeRequestIntegrationIde.infrastructure.service.RepositoryFileService

class CachedRepositoryFile(
    service: RepositoryFileService,
    private val cache: Cache
) : RepositoryFileDecorator(service) {
    private val myLogger = Logger.getInstance(this.javaClass)

    override fun findChanges(providerData: ProviderData, hashes: List<String>): List<Change> {
        val key = "${providerData.id}:${hashes.joinToString(",")}"
        return cache.getOrRun(key) {
            myLogger.info("Cache $key not found")
            val result = super.findChanges(providerData, hashes)

            cache.set(key, result, TIME_TO_LIVE)
            result
        }
    }

    companion object {
        const val TIME_TO_LIVE = 30000
    }
}