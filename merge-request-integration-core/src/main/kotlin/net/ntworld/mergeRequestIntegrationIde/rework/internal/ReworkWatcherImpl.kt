package net.ntworld.mergeRequestIntegrationIde.rework.internal

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx
import com.intellij.openapi.vcs.changes.Change
import com.intellij.openapi.vcs.changes.PreviewDiffVirtualFile
import com.intellij.openapi.vfs.LocalFileSystem
import git4idea.repo.GitRepository
import net.ntworld.mergeRequest.*
import net.ntworld.mergeRequestIntegrationIde.debug
import net.ntworld.mergeRequestIntegrationIde.infrastructure.ProjectServiceProvider
import net.ntworld.mergeRequestIntegrationIde.infrastructure.ReviewContext
import net.ntworld.mergeRequestIntegrationIde.infrastructure.internal.DiffPreviewProviderImpl
import net.ntworld.mergeRequestIntegrationIde.infrastructure.internal.ReviewContextImpl
import net.ntworld.mergeRequestIntegrationIde.rework.ReworkWatcher
import net.ntworld.mergeRequestIntegrationIde.task.FindMergeRequestTask
import net.ntworld.mergeRequestIntegrationIde.task.GetCommentsTask
import net.ntworld.mergeRequestIntegrationIde.task.GetCommitsTask

class ReworkWatcherImpl(
    override val projectServiceProvider: ProjectServiceProvider,
    override val repository: GitRepository,
    override val branchName: String,
    override val providerData: ProviderData,
    override val mergeRequestInfo: MergeRequestInfo
) : ReworkWatcher {
    private val myPreviewDiffVirtualFileMap = mutableMapOf<Change, PreviewDiffVirtualFile>()
    private var myTerminate = false
    private var myRunCount = 0
    private var myMergeRequest: MergeRequest? = null
    override var commits: List<Commit> = listOf()
    override var changes: List<Change> = listOf()
    override var comments: List<Comment> = listOf()
    override val interval: Long = 10000
    private val myGetCommitsTaskListener = object : GetCommitsTask.Listener {
        override fun dataReceived(mergeRequestInfo: MergeRequestInfo, commits: List<Commit>) {
            this@ReworkWatcherImpl.commits = commits
            changes = projectServiceProvider.repositoryFile.findChanges(providerData, commits.map { it.id })
            ApplicationManager.getApplication().invokeLater {
                projectServiceProvider.openSingleMRToolWindow {
                    projectServiceProvider.singleMRToolWindowNotifierTopic.requestShowChanges(
                        providerData, changes
                    )
                }
            }
        }
    }
    private val myGetCommentsTaskListener = object : GetCommentsTask.Listener {
        override fun dataReceived(
            providerData: ProviderData,
            mergeRequestInfo: MergeRequestInfo,
            comments: List<Comment>
        ) {
            this@ReworkWatcherImpl.comments = comments
        }
    }
    private val myFindMergeRequestTaskListener = object : FindMergeRequestTask.Listener {
        override fun dataReceived(mergeRequest: MergeRequest) {
            myMergeRequest = mergeRequest
        }
    }

    init {
        fetchCommits()
        fetchMergeRequest()
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
        val fileEditorManagerEx = FileEditorManagerEx.getInstanceEx(projectServiceProvider.project)
        myPreviewDiffVirtualFileMap.forEach { (_, diffFile) ->
            fileEditorManagerEx.closeFile(diffFile)
        }
        myPreviewDiffVirtualFileMap.clear()
        myTerminate = true
    }

    override fun openChange(change: Change) {
        val afterRevision = change.afterRevision
        if (null === afterRevision) {
            return openChangeAsDiffView(change)
        }

        val path = afterRevision.file.path
        val file = LocalFileSystem.getInstance().findFileByPath(path)
        if (null !== file) {
            FileEditorManagerEx.getInstanceEx(projectServiceProvider.project).openFile(file, true)
        }
    }

    private fun openChangeAsDiffView(change: Change) {
        val project = projectServiceProvider.project
        val diffFile = myPreviewDiffVirtualFileMap[change]
        if (null === diffFile) {
            val provider = DiffPreviewProviderImpl(project, change, buildReviewContext(), false)
            val created = PreviewDiffVirtualFile(provider)
            myPreviewDiffVirtualFileMap[change] = created
            FileEditorManagerEx.getInstanceEx(project).openFile(created, true)
        } else {
            FileEditorManagerEx.getInstanceEx(project).openFile(diffFile, true)
        }
    }

    private fun buildReviewContext(): ReviewContext? {
        val context = ReviewContextImpl(
            projectServiceProvider, providerData, mergeRequestInfo, projectServiceProvider.messageBus.connect()
        )
        val mr = myMergeRequest
        if (null !== mr) {
            context.diffReference = mr.diffReference
        }
        context.commits = commits
        context.reviewingCommits = commits
        context.changes = changes
        context.reviewingChanges = changes
        context.comments = comments
        return context
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

    private fun fetchMergeRequest() {
        debug("Fetching mergeRequest of ${providerData.id}:$branchName")
        val task = FindMergeRequestTask(
            projectServiceProvider = projectServiceProvider,
            providerData = providerData,
            mergeRequestInfo = mergeRequestInfo,
            listener = myFindMergeRequestTaskListener
        )
        task.start()
    }

    private fun updateComments() {
        debug("Fetching comments of ${providerData.id}:$branchName")
        val task = GetCommentsTask(
            projectServiceProvider = projectServiceProvider,
            providerData = providerData,
            mergeRequestInfo = mergeRequestInfo,
            listener = myGetCommentsTaskListener
        )
        task.start()
    }
}