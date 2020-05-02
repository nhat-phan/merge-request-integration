package net.ntworld.mergeRequestIntegrationIde.rework.internal

import com.intellij.openapi.vcs.changes.Change
import git4idea.repo.GitRepository
import net.ntworld.mergeRequest.*
import net.ntworld.mergeRequestIntegrationIde.debug
import net.ntworld.mergeRequestIntegrationIde.infrastructure.ProjectServiceProvider
import net.ntworld.mergeRequestIntegrationIde.rework.ReworkWatcher
import net.ntworld.mergeRequestIntegrationIde.task.GetCommitsTask

class ReworkWatcherImpl(
    override val projectServiceProvider: ProjectServiceProvider,
    override val repository: GitRepository,
    override val branchName: String,
    override val providerData: ProviderData,
    override val mergeRequestInfo: MergeRequestInfo
) : ReworkWatcher {
    private var myTerminate = false
    private var myFetchedCommits = false
    private var myFetchedComments = false
    private var myRunCount = 0
    override var commits: List<Commit> = listOf()
    override var changes: List<Change> = listOf()
    override var comments: List<Comment> = listOf()
    override val interval: Long = 10000
    private val myGetCommitsTaskListener = object : GetCommitsTask.Listener {
        override fun dataReceived(mergeRequestInfo: MergeRequestInfo, commits: List<Commit>) {
            this@ReworkWatcherImpl.commits = commits
            this@ReworkWatcherImpl.changes = projectServiceProvider.repositoryFile.findChanges(
                providerData, commits.map { it.id }
            )
            myFetchedCommits = true
        }
    }

    init {
        fetchCommits()
    }

    override fun canExecute(): Boolean {
        return !myTerminate
    }

    override fun shouldTerminate(): Boolean {
        return myTerminate || repository.currentBranchName != branchName || projectServiceProvider.isDoingCodeReview()
    }

    override fun execute() {
        if (myRunCount % 3 == 0) {
            updateComments()
        }

        myRunCount += 1
    }

    override fun terminate() {
        debug("ReworkWatcher of ${providerData.id}:$branchName is terminated")
        projectServiceProvider.reworkManager.markReworkWatcherTerminated(this)
    }

    override fun shutdown() {
        debug("Terminate ReworkWatcher of ${providerData.id}:$branchName")
        myTerminate = true
    }

    private fun fetchCommits() {
        debug("Fetching commits of ${providerData.id}:$branchName")
        val task = GetCommitsTask(
            projectServiceProvider = projectServiceProvider,
            providerData = providerData,
            mergeRequestInfo = mergeRequestInfo,
            listener = myGetCommitsTaskListener
        )
        task.start()
    }

    private fun updateComments() {
        debug("Fetching comments of ${providerData.id}:$branchName")
    }
}