package net.ntworld.mergeRequestIntegrationIde.ui.editor

import com.intellij.codeInsight.daemon.OutsidersPsiFileSupport
import com.intellij.codeInsight.daemon.impl.EditorTrackerListener
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.project.Project as IdeaProject
import com.intellij.testFramework.LightVirtualFile
import com.intellij.ui.content.ContentFactory
import net.ntworld.mergeRequestIntegration.update.UpdateManager
import net.ntworld.mergeRequestIntegrationIde.service.ApplicationService
import net.ntworld.mergeRequestIntegrationIde.task.GetAvailableUpdatesTask
import net.ntworld.mergeRequestIntegrationIde.ui.toolWindowTab.UpdateInfoTab

class EditorWatcher private constructor(
    private val applicationService: ApplicationService,
    private val ideaProject: IdeaProject,
    private val toolWindow: ToolWindow
) : EditorTrackerListener {
    private val projectService = applicationService.getProjectService(ideaProject)

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

    override fun activeEditorsChanged(activeEditors: MutableList<Editor>) {
        checkAvailableUpdates()
//        val codeReviewManager = projectService.codeReviewManager
//        if (null === codeReviewManager || null === codeReviewManager.repository) {
//            return
//        }
//
//        for (activeEditor in activeEditors) {
//            if (activeEditor !is EditorEx) {
//                continue
//            }
//            val file = activeEditor.virtualFile
//            if (file !is LightVirtualFile) {
//                continue
//            }
//
//            val path = OutsidersPsiFileSupport.getOriginalFilePath(file)
//            if (null === path || !path.startsWith(codeReviewManager.repository!!.root.path)) {
//                continue
//            }
//
//            val info = codeReviewManager.findChangeInfoByPathAndContent(
//                path, activeEditor.document.text
//            )
//            if (null === info) {
//                continue
//            }
//
//            val points = codeReviewManager.findCommentPoints(path, info)
//            points.forEach {
//                if (!it.comment.resolved) {
//                    EditorCommentManager.createPoint(applicationService, activeEditor, it)
//                }
//            }
//        }
    }

    private fun checkAvailableUpdates() {
        if (!UpdateManager.shouldGetAvailableUpdates()) {
            return
        }
        GetAvailableUpdatesTask(ideaProject, myGetAvailableUpdatesListener).start()
    }

    companion object {
        private var started: Boolean = false

        fun start(applicationService: ApplicationService, ideaProject: IdeaProject, toolWindow: ToolWindow) {
            return
            // TODO: Remove editor watcher completely
            if (!started) {
                started = true
                val instance = EditorWatcher(applicationService, ideaProject, toolWindow)
                ideaProject.messageBus.connect().subscribe(
                    EditorTrackerListener.TOPIC,
                    instance
                )
            }
        }
    }


}