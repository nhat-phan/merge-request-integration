package net.ntworld.mergeRequestIntegrationIde.rework

import git4idea.repo.GitRepository
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequestIntegrationIde.watcher.Watcher

interface BranchWatcher : Watcher {
    val providerData: ProviderData

    val repository: GitRepository

    val currentBranchName: String?

    fun shutdown()
}