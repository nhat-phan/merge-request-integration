package net.ntworld.mergeRequestIntegrationIde.infrastructure.service

import com.intellij.openapi.vcs.changes.Change
import net.ntworld.mergeRequest.ProviderData
import javax.swing.Icon

interface RepositoryFileService {

    fun findChanges(providerData: ProviderData, hashes: List<String>): List<Change>

    fun findIcon(providerData: ProviderData, path: String) : Icon

}