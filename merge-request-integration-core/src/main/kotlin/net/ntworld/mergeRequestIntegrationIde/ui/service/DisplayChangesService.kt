package net.ntworld.mergeRequestIntegrationIde.ui.service

import com.intellij.ide.ui.UISettings
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx
import com.intellij.openapi.vcs.changes.*
import com.intellij.openapi.project.Project as IdeaProject
import com.intellij.ui.tabs.JBTabsPosition
import com.intellij.vcs.log.Hash
import com.intellij.vcs.log.impl.VcsLogContentUtil
import com.intellij.vcs.log.impl.VcsLogManager
import com.intellij.vcs.log.util.VcsLogUtil
import git4idea.repo.GitRepository
import net.ntworld.mergeRequest.Commit
import net.ntworld.mergeRequest.MergeRequest
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequestIntegrationIde.infrastructure.ApplicationServiceProvider
import net.ntworld.mergeRequestIntegrationIde.util.RepositoryUtil
import javax.swing.SwingConstants
import kotlin.concurrent.thread

object DisplayChangesService {
    private var myTabPlacement: JBTabsPosition? = null
    private val myChangePreviewDiffVirtualFileMap = mutableMapOf<Change, PreviewDiffVirtualFile>()

    fun stop(ideaProject: IdeaProject, providerData: ProviderData, mergeRequest: MergeRequest) {
        closeAllDiffsAndRestoreTabPlacement(ideaProject)
        myTabPlacement = null
        myChangePreviewDiffVirtualFileMap.clear()
    }

    fun start(
        applicationServiceProvider: ApplicationServiceProvider,
        ideaProject: IdeaProject,
        providerData: ProviderData,
        mergeRequest: MergeRequest,
        commits: List<Commit>
    ) {
        if (myChangePreviewDiffVirtualFileMap.isNotEmpty()) {
            myChangePreviewDiffVirtualFileMap.clear()
        }
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
            thread {
                val hash = if (commits.isEmpty()) diff.headHash else commits.first().id
                displayChangesForOneCommit(
                    applicationServiceProvider,
                    ideaProject,
                    fileEditorManagerEx,
                    providerData,
                    mergeRequest,
                    repository,
                    log,
                    hash
                )
            }
        } else {
            thread {
                displayChangesForCommits(
                    applicationServiceProvider,
                    ideaProject,
                    fileEditorManagerEx,
                    providerData,
                    mergeRequest,
                    repository,
                    log,
                    commits
                )
            }
        }
        // rearrangeTabPlacement(fileEditorManagerEx)
    }

    private fun displayChangesForOneCommit(
        applicationServiceProvider: ApplicationServiceProvider,
        ideaProject: IdeaProject,
        fileEditorManagerEx: FileEditorManagerEx,
        providerData: ProviderData,
        mergeRequest: MergeRequest,
        repository: GitRepository,
        log: VcsLogManager,
        hash: String
    ) {
        // TODO: Reduce repetition
        val details = VcsLogUtil.getDetails(log.dataManager, repository.root, MyHash(hash))
        displayChanges(
            applicationServiceProvider,
            ideaProject,
            fileEditorManagerEx,
            providerData,
            mergeRequest,
            details.changes
        )
    }

    private fun displayChangesForCommits(
        applicationServiceProvider: ApplicationServiceProvider,
        ideaProject: IdeaProject,
        fileEditorManagerEx: FileEditorManagerEx,
        providerData: ProviderData,
        mergeRequest: MergeRequest,
        repository: GitRepository,
        log: VcsLogManager,
        commits: List<Commit>
    ) {
        // TODO: Reduce repetition
        val details = VcsLogUtil.getDetails(
            log.dataManager.getLogProvider(repository.root),
            repository.root,
            commits.map { it.id }
        )
        if (details.isEmpty()) {
            return
        }

        if (details.size == 1) {
            return displayChanges(
                applicationServiceProvider, ideaProject, fileEditorManagerEx,
                providerData, mergeRequest, details.first().changes
            )
        }

        val changes = VcsLogUtil.collectChanges(details) {
            it.changes
        }
        displayChanges(applicationServiceProvider, ideaProject, fileEditorManagerEx, providerData, mergeRequest, changes)
    }

    private fun displayChanges(
        applicationServiceProvider: ApplicationServiceProvider,
        ideaProject: IdeaProject,
        fileEditorManagerEx: FileEditorManagerEx,
        providerData: ProviderData,
        mergeRequest: MergeRequest,
        changes: Collection<Change>
    ) {
        applicationServiceProvider.findProjectServiceProvider(ideaProject).setCodeReviewChanges(providerData, mergeRequest, changes)

        ApplicationManager.getApplication().invokeLater {
            val reviewContext = applicationServiceProvider.findProjectServiceProvider(ideaProject).findReviewContextWhichDoingCodeReview()
            val max = applicationServiceProvider.settings.maxDiffChangesOpenedAutomatically
            if (null !== reviewContext && max != 0 && changes.size < max) {
                val limit = UISettings().editorTabLimit
                changes.forEachIndexed { index, item ->
                    if (index < limit) {
                        reviewContext.openChange(item, focus = true, displayMergeRequestId = false)
                    }
                }
            }
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

    // TODO: Will add an option in configuration then call later
    private fun rearrangeTabPlacement(fileEditorManagerEx: FileEditorManagerEx) {
        val editorWindows = fileEditorManagerEx.windows
        if (editorWindows.size == 1) {
            val editorWindow = editorWindows.first()
            myTabPlacement = editorWindow.tabbedPane.tabs.presentation.tabsPosition
            editorWindow.tabbedPane.setTabPlacement(SwingConstants.LEFT)
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
}