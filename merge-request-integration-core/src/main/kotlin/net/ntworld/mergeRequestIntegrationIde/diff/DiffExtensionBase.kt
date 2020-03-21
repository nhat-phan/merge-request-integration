package net.ntworld.mergeRequestIntegrationIde.diff

import com.intellij.diff.DiffContext
import com.intellij.diff.DiffExtension
import com.intellij.diff.FrameDiffTool
import com.intellij.diff.requests.DiffRequest
import com.intellij.diff.tools.fragmented.UnifiedDiffViewer
import com.intellij.diff.tools.simple.SimpleOnesideDiffViewer
import com.intellij.diff.tools.util.base.DiffViewerBase
import com.intellij.diff.tools.util.side.TwosideTextDiffViewer
import com.intellij.diff.util.DiffUserDataKeys
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project as IdeaProject
import com.intellij.openapi.vcs.changes.Change
import com.intellij.openapi.vcs.changes.actions.diff.ChangeDiffRequestProducer
import net.ntworld.mergeRequestIntegrationIde.service.ApplicationService
import net.ntworld.mergeRequestIntegrationIde.ui.util.Icons

open class DiffExtensionBase(
    private val applicationService: ApplicationService
) : DiffExtension() {

    override fun onViewerCreated(viewer: FrameDiffTool.DiffViewer, context: DiffContext, request: DiffRequest) {
        val presenter = createPresenter(viewer)
        if (null === presenter) {
            return
        }
        context.putUserData(
            DiffUserDataKeys.CONTEXT_ACTIONS, listOf(
                TestAction
            )
        )
    }

    private fun createPresenter(viewer: FrameDiffTool.DiffViewer): DiffPresenter? {
        return if (viewer is DiffViewerBase) {
            assertViewerIsValid(viewer) { project, change ->
                val model = DiffFactory.makeDiffModel(applicationService, project, change)
                val view = DiffFactory.makeView(applicationService, viewer, change)
                if (null !== view) {
                    DiffFactory.makeDiffPresenter(
                        applicationService = applicationService,
                        projectService = applicationService.getProjectService(project),
                        model = model,
                        view = view
                    )
                } else {
                    null
                }
            }
        } else null
    }

    private fun assertViewerIsValid(
        viewer: DiffViewerBase,
        invoker: ((IdeaProject, Change) -> DiffPresenter?)
    ): DiffPresenter? {
        val project = viewer.project
        val change = viewer.request.getUserData(ChangeDiffRequestProducer.CHANGE_KEY)
        if (null === change || null === project) {
            return null
        }
        return invoker.invoke(project, change)
    }

    private object TestAction : AnAction(null, null, Icons.Comments) {
        override fun actionPerformed(e: AnActionEvent) {
        }
    }
}