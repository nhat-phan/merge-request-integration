package net.ntworld.mergeRequestIntegrationIde.infrastructure.service

import com.intellij.openapi.vcs.changes.Change
import net.ntworld.mergeRequest.ProviderData

interface RepositoryFileService {

    fun findChanges(providerData: ProviderData, hashes: List<String>): List<Change>

}