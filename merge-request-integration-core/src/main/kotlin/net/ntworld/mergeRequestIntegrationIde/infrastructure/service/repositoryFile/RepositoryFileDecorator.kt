package net.ntworld.mergeRequestIntegrationIde.infrastructure.service.repositoryFile

import com.intellij.openapi.vcs.changes.Change
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequestIntegrationIde.infrastructure.service.RepositoryFileService

open class RepositoryFileDecorator(
    private val service: RepositoryFileService
) : RepositoryFileService {

    override fun findChanges(providerData: ProviderData, hashes: List<String>): List<Change> {
        return service.findChanges(providerData, hashes)
    }

}