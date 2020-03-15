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
import com.intellij.openapi.vcs.changes.ContentRevision
import com.intellij.openapi.vcs.changes.actions.diff.ChangeDiffRequestProducer
import net.ntworld.mergeRequestIntegrationIde.service.ApplicationService
import net.ntworld.mergeRequestIntegrationIde.service.CodeReviewManager
import net.ntworld.mergeRequestIntegrationIde.ui.util.Icons

open class DiffExtensionBase(
    private val applicationService: ApplicationService
) : DiffExtension() {

    override fun onViewerCreated(viewer: FrameDiffTool.DiffViewer, context: DiffContext, request: DiffRequest) {
        context.putUserData(
            DiffUserDataKeys.CONTEXT_ACTIONS, listOf(
                TestAction
            )
        )
        makePresenter(viewer)
    }

    private fun makePresenter(viewer: FrameDiffTool.DiffViewer): DiffPresenter? {
        return when (viewer) {
            is SimpleOnesideDiffViewer -> makeDiffPresenterForSimpleOneSideDiffViewer(viewer)
            is TwosideTextDiffViewer -> makeDiffPresenterForTwoSideTextDiffViewer(viewer)
            is UnifiedDiffViewer -> makeDiffPresenterForUnifiedDiffViewer(viewer)
            else -> null
        }
    }

    private fun assertViewerIsValid(
        viewer: DiffViewerBase,
        invoker: ((IdeaProject, Change) -> DiffPresenter)
    ): DiffPresenter? {
        val project = viewer.project
        val change = viewer.request.getUserData(ChangeDiffRequestProducer.CHANGE_KEY)
        if (null === change || null === project) {
            return null
        }
        return invoker.invoke(project, change)
    }

    private fun makeDiffPresenterForSimpleOneSideDiffViewer(viewer: SimpleOnesideDiffViewer): DiffPresenter? {
        return assertViewerIsValid(viewer) { project, change ->
            DiffPresenterImpl(
                projectService = applicationService.getProjectService(project),
                model = buildDiffModel(project, change),
                view = SimpleOneSideDiffView(applicationService, viewer, change, when(change.type) {
                    Change.Type.NEW -> DiffView.ContentType.AFTER
                    Change.Type.DELETED -> DiffView.ContentType.BEFORE
                    else -> throw Exception("Invalid change type")
                })
            )
        }
    }

    private fun makeDiffPresenterForTwoSideTextDiffViewer(viewer: TwosideTextDiffViewer): DiffPresenter? {
        return assertViewerIsValid(viewer) { project, change ->
            DiffPresenterImpl(
                projectService = applicationService.getProjectService(project),
                model = buildDiffModel(project, change),
                view = TwoSideTextDiffView(applicationService, viewer, change)
            )
        }
    }

    private fun makeDiffPresenterForUnifiedDiffViewer(viewer: UnifiedDiffViewer): DiffPresenter? {
        return assertViewerIsValid(viewer) { project, change ->
            DiffPresenterImpl(
                projectService = applicationService.getProjectService(project),
                model = buildDiffModel(project, change),
                view = UnifiedDiffView(applicationService, viewer, change)
            )
        }
    }

    private fun buildDiffModel(project: IdeaProject, change: Change): DiffModel {
        val codeReviewManager = applicationService.getProjectService(project).codeReviewManager
        if (null === codeReviewManager) {
            return DiffModelImpl(null, null, listOf(), change, listOf(), listOf())
        }
        val afterRevision = change.afterRevision
        val beforeRevision = change.beforeRevision

        if (null !== beforeRevision && null !== afterRevision) {
            val beforeChangeInfo = ChangeInfoImpl(change, beforeRevision, before = true, after = false)
            val afterChangeInfo = ChangeInfoImpl(change, afterRevision, before = false, after = true)
            return DiffModelImpl(
                codeReviewManager.providerData,
                codeReviewManager.mergeRequest,
                codeReviewManager.commits.toList(),
                change,
                commentsOnBeforeSide = codeReviewManager.findCommentPoints(
                    beforeChangeInfo.contentRevision.file.path,
                    beforeChangeInfo
                ),
                commentsOnAfterSide = codeReviewManager.findCommentPoints(
                    afterChangeInfo.contentRevision.file.path,
                    afterChangeInfo
                )
            )

        }

        if (null !== beforeRevision && null === afterRevision) {
            val changeInfo = ChangeInfoImpl(change, beforeRevision, before = true, after = false)
            return DiffModelImpl(
                codeReviewManager.providerData,
                codeReviewManager.mergeRequest,
                codeReviewManager.commits.toList(),
                change,
                commentsOnBeforeSide = codeReviewManager.findCommentPoints(
                    changeInfo.contentRevision.file.path,
                    changeInfo
                ),
                commentsOnAfterSide = listOf()
            )
        }

        if (null === beforeRevision && null !== afterRevision) {
            val changeInfo = ChangeInfoImpl(change, afterRevision, before = false, after = true)
            return DiffModelImpl(
                codeReviewManager.providerData,
                codeReviewManager.mergeRequest,
                codeReviewManager.commits.toList(),
                change,
                commentsOnBeforeSide = listOf(),
                commentsOnAfterSide = codeReviewManager.findCommentPoints(
                    changeInfo.contentRevision.file.path,
                    changeInfo
                )
            )
        }

        return DiffModelImpl(null, null, listOf(), change, listOf(), listOf())
    }

    private data class ChangeInfoImpl(
        override val change: Change,
        override val contentRevision: ContentRevision,
        override val before: Boolean,
        override val after: Boolean
    ) : CodeReviewManager.ChangeInfo

    private object TestAction : AnAction(null, null, Icons.Comments) {
        override fun actionPerformed(e: AnActionEvent) {
        }
    }
}