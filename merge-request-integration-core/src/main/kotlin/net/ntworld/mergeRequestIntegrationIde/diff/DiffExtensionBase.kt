package net.ntworld.mergeRequestIntegrationIde.diff

import com.intellij.diff.DiffContext
import com.intellij.diff.DiffExtension
import com.intellij.diff.FrameDiffTool
import com.intellij.diff.requests.DiffRequest
import com.intellij.diff.tools.util.base.DiffViewerBase
import com.intellij.diff.util.DiffUserDataKeys
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ToggleAction
import com.intellij.openapi.project.Project as IdeaProject
import com.intellij.openapi.vcs.changes.Change
import com.intellij.openapi.vcs.changes.actions.diff.ChangeDiffRequestProducer
import net.ntworld.mergeRequestIntegrationIde.infrastructure.ApplicationServiceProvider
import net.ntworld.mergeRequestIntegrationIde.component.Icons
import net.ntworld.mergeRequestIntegrationIde.infrastructure.ReviewContext
import net.ntworld.mergeRequestIntegrationIde.infrastructure.ProjectServiceProvider
import net.ntworld.mergeRequestIntegrationIde.util.TextChoiceUtil

open class DiffExtensionBase(
    private val applicationServiceProvider: ApplicationServiceProvider
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
                    applicationServiceProvider.settingsManager.displayCommentsInDiffView
                ),
                MyToggleResolvedCommentsAction(
                    presenter,
                    false
                ),
                MyShowDraftCommentsOnlyAction(
                    presenter,
                    false
                ),
                MyPublishDraftCommentsAction(
                    presenter
                )
            )
        )
    }

    private fun findReviewContext(projectServiceProvider: ProjectServiceProvider, request: DiffRequest): ReviewContext? {
        val reviewContext = request.getUserData(ReviewContext.KEY)
        if (null !== reviewContext) {
            return reviewContext
        }

        return projectServiceProvider.reviewContextManager.findDoingCodeReviewContext()
    }

    private fun createPresenter(viewer: FrameDiffTool.DiffViewer, request: DiffRequest): DiffPresenter? {
        return if (viewer is DiffViewerBase) {
            assertViewerIsValid(viewer) { project, change ->
                val projectServiceProvider = applicationServiceProvider.findProjectServiceProvider(project)
                val reviewContext = findReviewContext(projectServiceProvider, request)
                if (null === reviewContext) {
                    return@assertViewerIsValid null
                }

                val model = DiffFactory.makeDiffModel(projectServiceProvider, reviewContext, change)
                val view = DiffFactory.makeView(projectServiceProvider, viewer, change)
                if (null !== view && null !== model) {
                    DiffFactory.makeDiffPresenter(
                        projectServiceProvider = projectServiceProvider,
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

    private class MyPublishDraftCommentsAction(
        private val presenter: DiffPresenter
    ) : AnAction("Publish draft comments", "Publish all draft comments in this diff view", null) {
        override fun actionPerformed(e: AnActionEvent) {
            presenter.publishAllDraftComments()
        }

        override fun update(e: AnActionEvent) {
            if (presenter.model.draftComments.isNotEmpty()) {
                e.presentation.isVisible = true
                e.presentation.text = "Publish " + TextChoiceUtil.draftComment(presenter.model.draftComments.count())
            } else {
                e.presentation.isVisible = false
            }
        }

        override fun displayTextInToolbar() = true
    }

    private class MyShowDraftCommentsOnlyAction(
        private val presenter: DiffPresenter,
        initializedState: Boolean
    ) : ToggleAction("Draft Comments Only", "Only show draft comments", Icons.Edit) {
        private var myOnlyDraft = initializedState

        override fun isSelected(e: AnActionEvent): Boolean {
            return myOnlyDraft
        }

        override fun setSelected(e: AnActionEvent, state: Boolean) {
            myOnlyDraft = state
            presenter.model.rebuildCommentsWhenOnlyShowDraftChanged(myOnlyDraft)
        }

        override fun update(e: AnActionEvent) {
            super.update(e)
            e.presentation.isVisible = presenter.model.draftComments.isNotEmpty()
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
            presenter.model.rebuildCommentsWhenShowResolvedChanged(myShown)
        }
    }
}