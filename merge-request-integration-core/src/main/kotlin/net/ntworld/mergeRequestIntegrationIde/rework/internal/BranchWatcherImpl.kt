package net.ntworld.mergeRequestIntegrationIde.rework.internal

import git4idea.repo.GitRepository
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequestIntegrationIde.DEBUG
import net.ntworld.mergeRequestIntegrationIde.rework.BranchWatcher
import net.ntworld.mergeRequestIntegrationIde.rework.ReworkManager

class BranchWatcherImpl(
    val reworkManager: ReworkManager,
    override val providerData: ProviderData,
    override val repository: GitRepository
) : BranchWatcher {
    private var myTerminate = false
    private var myPrevBranchName = repository.currentBranchName

    override val currentBranchName: String?
        get() = repository.currentBranchName

    override val interval: Long = 3000

    override fun canExecute(): Boolean {
        return true
    }

    override fun shouldTerminate(): Boolean {
        return myTerminate
    }

    override fun execute() {
        val branchName = currentBranchName
        if (null !== branchName && myPrevBranchName != branchName) {
            myPrevBranchName = branchName
            if (DEBUG) println("BranchWatcher of ${providerData.id} found branch changed, request create watcher")
            reworkManager.requestCreateReworkWatcher(providerData, branchName)
        }
    }

    override fun terminate() {
        if (DEBUG) println("BranchWatcher of ${providerData.id} is terminated")
    }

    override fun shutdown() {
        if (DEBUG) println("Terminate BranchWatcher of ${providerData.id}")
        myTerminate = true
    }
}