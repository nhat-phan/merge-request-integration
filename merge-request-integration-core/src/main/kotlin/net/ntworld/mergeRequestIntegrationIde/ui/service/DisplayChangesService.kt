package net.ntworld.mergeRequestIntegrationIde.ui.service

import com.intellij.diff.chains.DiffRequestProducer
import com.intellij.diff.impl.CacheDiffRequestProcessor
import com.intellij.diff.impl.DiffRequestProcessor
import com.intellij.diff.requests.NoDiffRequest
import com.intellij.diff.util.DiffPlaces
import com.intellij.ide.ui.UISettings
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
import net.ntworld.mergeRequestIntegrationIde.service.ApplicationService
import net.ntworld.mergeRequestIntegrationIde.service.ProjectService
import net.ntworld.mergeRequestIntegrationIde.ui.util.RepositoryUtil
import javax.swing.SwingConstants

object DisplayChangesService {
    private var myTabPlacement: JBTabsPosition? = null
    private val myChangePreviewDiffVirtualFileMap = mutableMapOf<Change, PreviewDiffVirtualFile>()

    fun stop(ideaProject: IdeaProject, providerData: ProviderData, mergeRequest: MergeRequest) {
        closeAllDiffsAndRestoreTabPlacement(ideaProject)
        myTabPlacement = null
        myChangePreviewDiffVirtualFileMap.clear()
    }

    fun start(
        applicationService: ApplicationService,
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
            val hash = if (commits.isEmpty()) diff.headHash else commits.first().id
            displayChangesForOneCommit(
                applicationService,
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
                applicationService,
                ideaProject,
                fileEditorManagerEx,
                providerData,
                mergeRequest,
                repository,
                log,
                commits
            )
        }
        // rearrangeTabPlacement(fileEditorManagerEx)
    }

    private fun displayChangesForOneCommit(
        applicationService: ApplicationService,
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
            applicationService,
            ideaProject,
            fileEditorManagerEx,
            providerData,
            mergeRequest,
            details.changes
        )
    }

    private fun displayChangesForCommits(
        applicationService: ApplicationService,
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
                applicationService, ideaProject, fileEditorManagerEx,
                providerData, mergeRequest, details.first().changes
            )
        }

        val changes = VcsLogUtil.collectChanges(details) {
            it.changes
        }
        displayChanges(applicationService, ideaProject, fileEditorManagerEx, providerData, mergeRequest, changes)
    }

    fun findChanges(ideaProject: IdeaProject, providerData: ProviderData, hashes: List<String>): List<Change> {
        try {
            val repository = RepositoryUtil.findRepository(ideaProject, providerData)
            if (null === repository) {
                return listOf()
            }
            val log = VcsLogContentUtil.getOrCreateLog(ideaProject)
            if (null === log) {
                return listOf()
            }

            val details = VcsLogUtil.getDetails(
                log.dataManager.getLogProvider(repository.root),
                repository.root,
                hashes
            )
            return VcsLogUtil.collectChanges(details) { it.changes }
        } catch (exception: Exception) {
            return listOf()
        }
    }

    private fun displayChanges(
        applicationService: ApplicationService,
        ideaProject: IdeaProject,
        fileEditorManagerEx: FileEditorManagerEx,
        providerData: ProviderData,
        mergeRequest: MergeRequest,
        changes: Collection<Change>
    ) {
        applicationService.getProjectService(ideaProject).setCodeReviewChanges(providerData, mergeRequest, changes)
        val limit = UISettings().editorTabLimit
        changes.forEachIndexed { index, item ->
            if (index < limit) {
                openChange(ideaProject, fileEditorManagerEx, item)
            }
        }
    }

    fun openChange(
        ideaProject: IdeaProject,
        fileEditorManagerEx: FileEditorManagerEx,
        change: Change,
        focus: Boolean = false
    ) {
        try {
            val diffFile = myChangePreviewDiffVirtualFileMap[change]
            if (null === diffFile) {
                val provider = MyDiffPreviewProvider(ideaProject, change)
                val created = PreviewDiffVirtualFile(provider)
                myChangePreviewDiffVirtualFileMap[change] = created
                fileEditorManagerEx.openFile(created, focus)
            } else {
                fileEditorManagerEx.openFile(diffFile, focus)
            }
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