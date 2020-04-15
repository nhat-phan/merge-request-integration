package net.ntworld.mergeRequestIntegrationIde.diff

import com.intellij.diff.DiffContext
import com.intellij.diff.DiffExtension
import com.intellij.diff.FrameDiffTool
import com.intellij.diff.requests.DiffRequest
import com.intellij.diff.tools.util.base.DiffViewerBase
import com.intellij.diff.util.DiffUserDataKeys
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ToggleAction
import com.intellij.openapi.project.Project as IdeaProject
import com.intellij.openapi.vcs.changes.Change
import com.intellij.openapi.vcs.changes.actions.diff.ChangeDiffRequestProducer
import net.ntworld.mergeRequestIntegrationIde.service.ApplicationService
import net.ntworld.mergeRequestIntegrationIde.component.Icons
import net.ntworld.mergeRequestIntegrationIde.infrastructure.ReviewContext
import net.ntworld.mergeRequestIntegrationIde.infrastructure.ReviewContextManager
import net.ntworld.mergeRequestIntegrationIde.service.ProjectService

open class DiffExtensionBase(
    private val applicationService: ApplicationService
) : DiffExtension() {

    override fun onViewerCreated(viewer: FrameDiffTool.DiffViewer, context: DiffContext, request: DiffRequest) {
        val presenter = createPresenter(viewer, request)
        if (null === presenter) {
            return
        }
        context.putUserData(
            DiffUserDataKeys.CONTEXT_ACTIONS, listOf(
                MyToggleAllCommentsAction(
                    presenter,
                    applicationService.settings.displayCommentsInDiffView
                ),
                MyToggleResolvedCommentsAction(
                    presenter,
                    false
                )
            )
        )
    }

    private fun findReviewContext(projectService: ProjectService, request: DiffRequest): ReviewContext? {
        val reviewContext = request.getUserData(ReviewContext.KEY)
        if (null !== reviewContext) {
            return reviewContext
        }

        return if (projectService.isDoingCodeReview()) {
            ReviewContextManager.findSelectedContext()
        } else null
    }

    private fun createPresenter(viewer: FrameDiffTool.DiffViewer, request: DiffRequest): DiffPresenter? {
        return if (viewer is DiffViewerBase) {
            assertViewerIsValid(viewer) { project, change ->
                val projectService = applicationService.getProjectService(project)
                val reviewContext = findReviewContext(projectService, request)
                if (null === reviewContext) {
                    return@assertViewerIsValid null
                }

                val model = DiffFactory.makeDiffModel(projectService, reviewContext, change)
                val view = DiffFactory.makeView(applicationService, viewer, change)
                if (null !== view && null !== model) {
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

    private class MyToggleAllCommentsAction(
        private val presenter: DiffPresenter,
        initializedState: Boolean
    ) : ToggleAction("Toggle Comments", "Toggle all comments in this diff view", Icons.Comments) {
        private var myShowAll = initializedState

        override fun isSelected(e: AnActionEvent): Boolean {
            return myShowAll
        }

        override fun setSelected(e: AnActionEvent, state: Boolean) {
            myShowAll = state
            if (myShowAll) {
                presenter.view.showAllComments()
            } else {
                presenter.view.hideAllComments()
            }
        }
    }

    private class MyToggleResolvedCommentsAction(
        private val presenter: DiffPresenter,
        initializedState: Boolean
    ) : ToggleAction("Show Resolved Comments", "Toggle resolved comments in this diff view", Icons.Resolved) {
        private var myShown = initializedState

        override fun isSelected(e: AnActionEvent): Boolean {
            return myShown
        }

        override fun setSelected(e: AnActionEvent, state: Boolean) {
            myShown = state
            presenter.model.rebuildComments(myShown)
        }
    }
}