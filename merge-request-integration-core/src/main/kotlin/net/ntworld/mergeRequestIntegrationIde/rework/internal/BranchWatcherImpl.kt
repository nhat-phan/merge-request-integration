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
        return !projectServiceProvider.project.isDisposed
    }

    override fun shouldTerminate(): Boolean {
        return myTerminate ||
            projectServiceProvider.project.isDisposed ||
            projectServiceProvider.isDoingCodeReview() ||
            !projectServiceProvider.applicationSettings.enableReworkProcess
    }

    override fun execute() {
        val branchName = currentBranchName
        if (null !== branchName && myPrevBranchName != branchName) {
            myPrevBranchName = branchName
            debug("${providerData.id}: BranchWatcher found branch changed, request create watcher")
            reworkManager.requestCreateReworkWatcher(providerData, repository, branchName)
        }
    }

    override fun terminate() {
        debug("${providerData.id}: BranchWatcher is terminated")
        reworkManager.markBranchWatcherTerminated(this)
    }

    override fun shutdown() {
        debug("${providerData.id}: terminate BranchWatcher")
        myTerminate = true
    }
}