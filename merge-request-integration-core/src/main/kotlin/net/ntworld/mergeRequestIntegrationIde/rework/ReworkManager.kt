package net.ntworld.mergeRequestIntegrationIde.rework

import com.intellij.openapi.vcs.changes.Change
import git4idea.repo.GitRepository
import net.ntworld.mergeRequest.ProviderData

interface ReworkManager {

    fun clear()

    fun markReworkWatcherTerminated(reworkWatcher: ReworkWatcher)

    fun createBranchWatcher(providerData: ProviderData)

    fun requestCreateReworkWatcher(providers: List<ProviderData>, branchName: String)

    fun requestCreateReworkWatcher(providerData: ProviderData, repository: GitRepository, branchName: String)

    fun findReworkWatcherByChange(providerData: ProviderData, change: Change): ReworkWatcher?

    fun findActiveReworkWatcher(providerData: ProviderData): ReworkWatcher?
}