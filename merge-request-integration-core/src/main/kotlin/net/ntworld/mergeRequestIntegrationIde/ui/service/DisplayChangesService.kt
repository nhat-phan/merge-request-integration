package net.ntworld.mergeRequestIntegrationIde.ui.service

import com.intellij.diff.chains.DiffRequestProducer
import com.intellij.diff.impl.CacheDiffRequestProcessor
import com.intellij.diff.impl.DiffRequestProcessor
import com.intellij.diff.requests.NoDiffRequest
import com.intellij.diff.util.DiffPlaces
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx
import com.intellij.openapi.vcs.changes.*
import com.intellij.openapi.project.Project as IdeaProject
import com.intellij.ui.tabs.JBTabsPosition
import com.intellij.ui.tabs.impl.JBTabsImpl
import com.intellij.vcs.log.Hash
import com.intellij.vcs.log.impl.VcsLogContentUtil
import com.intellij.vcs.log.impl.VcsLogManager
import com.intellij.vcs.log.ui.frame.VcsLogChangesBrowser
import com.intellij.vcs.log.util.VcsLogUtil
import git4idea.repo.GitRepository
import net.ntworld.mergeRequest.Commit
import net.ntworld.mergeRequest.MergeRequest
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequestIntegrationIde.service.ProjectService
import net.ntworld.mergeRequestIntegrationIde.ui.util.RepositoryUtil
import javax.swing.SwingConstants

object DisplayChangesService {
    private var myTabPlacement: JBTabsPosition? = null

    fun stop(ideaProject: IdeaProject, providerData: ProviderData, mergeRequest: MergeRequest) {
        closeAllDiffsAndRestoreTabPlacement(ideaProject)
        myTabPlacement = null
    }

    fun start(ideaProject: IdeaProject, providerData: ProviderData, mergeRequest: MergeRequest, commits: List<Commit>) {
        myTabPlacement = null
        val diff = mergeRequest.diffReference
        val repository = RepositoryUtil.findRepository(ideaProject, providerData)
        if (null === repository || null === diff) {
            return
        }
        val log = VcsLogContentUtil.getOrCreateLog(ideaProject)
        if (null === log) {
            return
        }

        val fileEditorManagerEx = FileEditorManagerEx.getInstanceEx(ideaProject)
        if (commits.size <= 1) {
            val hash = if (commits.isEmpty()) diff.headHash else commits.first().id
            displayChangesForOneCommit(
                ideaProject,
                fileEditorManagerEx,
                providerData,
                mergeRequest,
                repository,
                log,
                hash
            )
        } else {
            displayChangesForCommits(
                ideaProject,
                fileEditorManagerEx,
                providerData,
                mergeRequest,
                repository,
                log,
                commits
            )
        }
        removeCloseButtons(fileEditorManagerEx)
    }

    private fun displayChangesForOneCommit(
        ideaProject: IdeaProject,
        fileEditorManagerEx: FileEditorManagerEx,
        providerData: ProviderData,
        mergeRequest: MergeRequest,
        repository: GitRepository,
        log: VcsLogManager,
        hash: String
    ) {
        val details = VcsLogUtil.getDetails(log.dataManager, repository.root, MyHash(hash))
        displayChanges(ideaProject, fileEditorManagerEx, providerData, mergeRequest, details.changes)
    }

    private fun displayChangesForCommits(
        ideaProject: IdeaProject,
        fileEditorManagerEx: FileEditorManagerEx,
        providerData: ProviderData,
        mergeRequest: MergeRequest,
        repository: GitRepository,
        log: VcsLogManager,
        commits: List<Commit>
    ) {
        val details = VcsLogUtil.getDetails(
            log.dataManager.getLogProvider(repository.root),
            repository.root,
            commits.map { it.id }
        )
        if (details.isEmpty()) {
            return
        }

        if (details.size == 1) {
            return displayChanges(ideaProject, fileEditorManagerEx, providerData, mergeRequest, details.first().changes)
        }

        val changes = VcsLogUtil.collectChanges(details) {
            it.changes
        }
        displayChanges(ideaProject, fileEditorManagerEx, providerData, mergeRequest, changes)
    }

    private fun displayChanges(
        ideaProject: IdeaProject,
        fileEditorManagerEx: FileEditorManagerEx,
        providerData: ProviderData,
        mergeRequest: MergeRequest,
        changes: Collection<Change>
    ) {
        ProjectService.getInstance(ideaProject).setCodeReviewChanges(providerData, mergeRequest, changes)
        changes.forEach { openChange(ideaProject, fileEditorManagerEx, it) }
    }

    private fun openChange(
        ideaProject: IdeaProject,
        fileEditorManagerEx: FileEditorManagerEx,
        change: Change
    ) {
        try {
            val provider = MyDiffPreviewProvider(ideaProject, change)
            val diffFile = PreviewDiffVirtualFile(provider)
            fileEditorManagerEx.openFile(diffFile, false)
        } catch (exception: Exception) {
            println(exception)
        }
    }

    private fun closeAllDiffsAndRestoreTabPlacement(ideaProject: IdeaProject) {
        val fileEditorManagerEx = FileEditorManagerEx.getInstanceEx(ideaProject)
        val openFiles = fileEditorManagerEx.openFiles
        for (openFile in openFiles) {
            fileEditorManagerEx.closeFile(openFile)
        }

        val tabPlacement = myTabPlacement
        if (null !== tabPlacement) {
            val editorWindows = fileEditorManagerEx.windows
            if (editorWindows.size == 1) {
                val editorWindow = editorWindows.first()
                editorWindow.tabbedPane.setTabPlacement(
                    when (tabPlacement) {
                        JBTabsPosition.top -> SwingConstants.TOP
                        JBTabsPosition.left -> SwingConstants.LEFT
                        JBTabsPosition.bottom -> SwingConstants.BOTTOM
                        JBTabsPosition.right -> SwingConstants.RIGHT
                    }
                )
            }
        }
    }

    private fun removeCloseButtons(fileEditorManagerEx: FileEditorManagerEx) {
        val editorWindows = fileEditorManagerEx.windows
        if (editorWindows.size == 1) {
            val editorWindow = editorWindows.first()
            myTabPlacement = editorWindow.tabbedPane.tabs.presentation.tabsPosition
            editorWindow.tabbedPane.setTabPlacement(SwingConstants.LEFT)
            val tabs = editorWindow.tabbedPane.tabs
            if (tabs is JBTabsImpl) {
                for (entry in tabs.myInfo2Label) {
                    entry.value.setActionPanelVisible(false)
                }
            }
        }
    }

    class MyHash(private val hash: String) : Hash {
        override fun toShortString(): String {
            return hash.substring(0, 6)
        }

        override fun asString(): String {
            return hash
        }
    }

    class MyDiffPreviewProvider(
        private val project: IdeaProject,
        val change: Change
    ) : DiffPreviewProvider {
        override fun getOwner(): Any {
            return this
        }

        override fun createDiffRequestProcessor(): DiffRequestProcessor {
            return MyDiffRequestProcessor(project, change)
        }

        override fun getEditorTabName(): String {
            return ChangesUtil.getFilePath(change).name
        }

    }

    class MyDiffRequestProcessor(
        project: IdeaProject,
        private val change: Change?
    ) : CacheDiffRequestProcessor.Simple(project, DiffPlaces.DEFAULT), DiffPreviewUpdateProcessor {
        override fun clear() {
            applyRequest(NoDiffRequest.INSTANCE, false, null)
        }

        override fun getFastLoadingTimeMillis(): Int {
            return 10
        }

        override fun refresh(fromModelRefresh: Boolean) {
            updateRequest()
        }

        override fun getCurrentRequestProvider(): DiffRequestProducer? {
            return if (null === change) {
                null
            } else {
                VcsLogChangesBrowser.createDiffRequestProducer(project!!, change, HashMap(), true)
            }
        }
    }
}