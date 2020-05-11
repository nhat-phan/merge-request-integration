package net.ntworld.mergeRequestIntegrationIde.rework.internal

import git4idea.repo.GitRepository
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequestIntegrationIde.debug
import net.ntworld.mergeRequestIntegrationIde.infrastructure.ProjectServiceProvider
import net.ntworld.mergeRequestIntegrationIde.rework.BranchWatcher
import net.ntworld.mergeRequestIntegrationIde.rework.ReworkManager

class BranchWatcherImpl(
    private val projectServiceProvider: ProjectServiceProvider,
    private val reworkManager: ReworkManager,
    override val providerData: ProviderData,
    override val repository: GitRepository
) : BranchWatcher {
    private var myTerminate = false
    private var myPrevBranchName: String? = null

    override val currentBranchName: String?
        get() = repository.currentBranchName

    override val interval: Long = 3000

    override fun canExecute(): Boolean {
        return true
    }

    override fun shouldTerminate(): Boolean {
        return myTerminate || projectServiceProvider.isDoingCodeReview()
    }

    override fun execute() {
        val branchName = currentBranchName
        if (null !== branchName && myPrevBranchName != branchName) {
            myPrevBranchName = branchName
            debug("BranchWatcher of ${providerData.id} found branch changed, request create watcher")
            reworkManager.requestCreateReworkWatcher(providerData, repository, branchName)
        }
    }

    override fun terminate() {
        debug("BranchWatcher of ${providerData.id} is terminated")
        reworkManager.markBranchWatcherTerminated(this)
    }

    override fun shutdown() {
        debug("Terminate BranchWatcher of ${providerData.id}")
        myTerminate = true
    }
}