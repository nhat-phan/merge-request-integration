package net.ntworld.mergeRequestIntegrationIde.ui.editor

import com.intellij.codeInsight.daemon.OutsidersPsiFileSupport
import com.intellij.codeInsight.daemon.impl.EditorTrackerListener
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.vcs.changes.ContentRevision
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.project.Project as IdeaProject
import com.intellij.testFramework.LightVirtualFile
import com.intellij.ui.content.ContentFactory
import git4idea.repo.GitRepository
import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequest.MergeRequest
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequestIntegration.update.UpdateManager
import net.ntworld.mergeRequestIntegrationIde.service.ProjectEventListener
import net.ntworld.mergeRequestIntegrationIde.service.ProjectService
import net.ntworld.mergeRequestIntegrationIde.task.GetAvailableUpdatesTask
import net.ntworld.mergeRequestIntegrationIde.ui.toolWindowTab.HomeToolWindowTab
import net.ntworld.mergeRequestIntegrationIde.ui.toolWindowTab.UpdateInfoTab
import net.ntworld.mergeRequestIntegrationIde.ui.util.RepositoryUtil

class EditorWatcher private constructor(
    private val ideaProject: IdeaProject,
    private val toolWindow: ToolWindow
) : ProjectEventListener, EditorTrackerListener {
    private val projectService = ProjectService.getInstance(ideaProject)
    private val myCommentsHash = mutableMapOf<String, MutableList<Comment>>()
    private var myRepository: GitRepository? = null

    private val myGetAvailableUpdatesListener = object: GetAvailableUpdatesTask.Listener {
        override fun dataReceived(updates: List<String>) {
            if (updates.isEmpty()) {
                return
            }
            ApplicationManager.getApplication().invokeLater {
                val content = toolWindow.contentManager.findContent("New Update is Available")
                if (null === content) {
                    val tab = ContentFactory.SERVICE.getInstance().createContent(
                        UpdateInfoTab(updates).createComponent(),
                        "New Update is Available",
                        true
                    )
                    toolWindow.contentManager.addContent(tab)
                }
            }
        }
    }

    init {
        projectService.dispatcher.addListener(this)
    }

    override fun codeReviewCommentsSet(
        providerData: ProviderData,
        mergeRequest: MergeRequest,
        comments: Collection<Comment>
    ) {
        val repository = myRepository
        if (null === repository) {
            return
        }
        for (comment in comments) {
            val position = comment.position
            if (null === position) {
                continue
            }
            if (null !== position.newPath) {
                doHashComment(repository, position.newPath!!, comment)
            }
            if (null !== position.oldPath) {
                doHashComment(repository, position.oldPath!!, comment)
            }
        }
    }

    private fun doHashComment(repository: GitRepository, path: String, comment: Comment) {
        val fullPath = RepositoryUtil.findAbsolutePath(repository, path)
        val list = myCommentsHash[fullPath]
        if (null === list) {
            myCommentsHash[fullPath] = mutableListOf(comment)
        } else {
            if (!list.contains(comment)) {
                list.add(comment)
            }
        }
    }

    override fun startCodeReview(providerData: ProviderData, mergeRequest: MergeRequest) {
        myRepository = RepositoryUtil.findRepository(ideaProject, providerData)
        codeReviewCommentsSet(providerData, mergeRequest, projectService.getCodeReviewComments())
        codeReviewChangesSet(providerData, mergeRequest, projectService.getCodeReviewChanges())
    }

    override fun stopCodeReview(providerData: ProviderData, mergeRequest: MergeRequest) {
        myRepository = null
        myCommentsHash.clear()
    }

    override fun activeEditorsChanged(activeEditors: MutableList<Editor>) {
        checkAvailableUpdates()
        if (!projectService.isDoingCodeReview()) {
            return
        }
        val repository = myRepository
        if (null === repository) {
            return
        }

        for (activeEditor in activeEditors) {
            if (activeEditor !is EditorEx) {
                continue
            }
            val file = activeEditor.virtualFile
            if (file !is LightVirtualFile) {
                continue
            }

            val path = OutsidersPsiFileSupport.getOriginalFilePath(file)
            if (null === path || !path.startsWith(repository.root.path)) {
                continue
            }

//            val revision = projectService.documentChangeService.guessChangeRevisionOfDocument(
//                path, activeEditor.document
//            )
//            if (null !== revision) {
//                displayCommentsForRevision(path, activeEditor, revision.contentRevision, revision.before)
//            }
        }
    }

    private fun displayCommentsForRevision(path: String, editor: EditorEx, revision: ContentRevision, old: Boolean) {
        val comments = myCommentsHash[path]
        if (null === comments || comments.isEmpty()) {
            return
        }

        for (comment in comments) {
            val position = comment.position!!
            if (position.headHash == revision.revisionNumber.asString()) {
                if (old) {
                    println("comment ${position.oldLine} ${comment.body} ")
                } else {
                    println("comment ${position.newLine} ${comment.body} ")
                }
            }
        }
    }

    private fun checkAvailableUpdates() {
        if (!UpdateManager.shouldGetAvailableUpdates()) {
            return
        }
        GetAvailableUpdatesTask(ideaProject, myGetAvailableUpdatesListener).start()
    }

    companion object {
        private var started: Boolean = false

        fun start(ideaProject: IdeaProject, toolWindow: ToolWindow) {
            if (!started) {
                started = true
                val instance = EditorWatcher(ideaProject, toolWindow)
                ideaProject.messageBus.connect().subscribe(
                    EditorTrackerListener.TOPIC,
                    instance
                )
            }
        }
    }


}