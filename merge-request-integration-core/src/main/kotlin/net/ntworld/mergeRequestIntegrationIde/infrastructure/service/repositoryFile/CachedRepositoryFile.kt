package net.ntworld.mergeRequestIntegrationIde.infrastructure.service.repositoryFile

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.vcs.changes.Change
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequest.api.Cache
import net.ntworld.mergeRequestIntegrationIde.infrastructure.service.RepositoryFileService
import javax.swing.Icon

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

            cache.set(key, result, FIND_CHANGES_TTL)
            result
        }
    }

    override fun findIcon(providerData: ProviderData, path: String): Icon {
        val key = "${providerData.id}:icon:${path}"
        return cache.getOrRun(key) {
            myLogger.info("Cache $key not found")
            val result = super.findIcon(providerData, path)

            cache.set(key, result, FIND_ICON_TTL)
            result
        }
    }

    companion object {
        const val FIND_CHANGES_TTL = 30000
        const val FIND_ICON_TTL = 60000
    }
}