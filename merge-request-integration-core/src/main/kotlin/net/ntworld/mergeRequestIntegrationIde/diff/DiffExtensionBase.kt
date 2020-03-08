package net.ntworld.mergeRequestIntegrationIde.diff

import com.intellij.diff.DiffContext
import com.intellij.diff.DiffExtension
import com.intellij.diff.FrameDiffTool
import com.intellij.diff.requests.DiffRequest
import com.intellij.diff.tools.fragmented.UnifiedDiffViewer
import com.intellij.diff.tools.simple.SimpleOnesideDiffViewer
import com.intellij.diff.tools.util.base.DiffViewerBase
import com.intellij.diff.tools.util.side.TwosideTextDiffViewer
import com.intellij.openapi.project.Project as IdeaProject
import com.intellij.openapi.vcs.changes.Change
import com.intellij.openapi.vcs.changes.ContentRevision
import com.intellij.openapi.vcs.changes.actions.diff.ChangeDiffRequestProducer
import net.ntworld.mergeRequestIntegrationIde.service.ApplicationService
import net.ntworld.mergeRequestIntegrationIde.service.CodeReviewManager

open class DiffExtensionBase(
    private val applicationService: ApplicationService
) : DiffExtension() {

    override fun onViewerCreated(viewer: FrameDiffTool.DiffViewer, context: DiffContext, request: DiffRequest) {
        makePresenter(viewer)
    }

    internal fun makePresenter(viewer: FrameDiffTool.DiffViewer): DiffPresenter? {
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
            val codeReviewManager = applicationService.getProjectService(project).codeReviewManager
            val model = if (null === codeReviewManager) {
                DiffModelImpl(null, change, listOf(), listOf())
            } else {
                val changeInfo = when (change.type) {
                    Change.Type.MODIFICATION -> throw Exception("Not supported")
                    Change.Type.NEW -> ChangeInfoImpl(change, change.afterRevision!!, before = false, after = true)
                    Change.Type.DELETED -> ChangeInfoImpl(change, change.afterRevision!!, before = true, after = false)
                    Change.Type.MOVED -> throw Exception("Not supported")
                }
                val commentPoints = codeReviewManager.findCommentPoints(
                    changeInfo.contentRevision.file.path,
                    changeInfo
                )
                DiffModelImpl(
                    codeReviewManager.mergeRequest,
                    change,
                    commentsOnAfterSide = if (changeInfo.after) commentPoints else listOf(),
                    commentsOnBeforeSide = if (changeInfo.before) commentPoints else listOf()
                )
            }

            DiffPresenterImpl(
                model = model,
                view = SimpleOneSideDiffView(applicationService, viewer)
            )
        }
    }

    // Fixme: Remove after implement for each type of diff viewer
    private fun makeDiffPresenterForTwoSideTextDiffViewer(viewer: TwosideTextDiffViewer): DiffPresenterImpl? {
        val project = viewer.project
        val change = viewer.request.getUserData(ChangeDiffRequestProducer.CHANGE_KEY)
        return if (null !== change && null !== project) {
            DiffPresenterImpl(
                model = makeModel(project, change),
                view = TwoSideTextDiffView(applicationService, viewer)
            )
        } else {
            null
        }
    }

    // Fixme: Remove after implement for each type of diff viewer
    private fun makeDiffPresenterForUnifiedDiffViewer(viewer: UnifiedDiffViewer): DiffPresenterImpl? {
        val project = viewer.project
        val change = viewer.request.getUserData(ChangeDiffRequestProducer.CHANGE_KEY)
        return if (null !== change && null !== project) {
            DiffPresenterImpl(
                model = makeModel(project, change),
                view = UnifiedDiffView(applicationService, viewer)
            )
        } else {
            null
        }
    }

    // Fixme: Remove after implement for each type of diff viewer
    private fun makeModel(project: IdeaProject, change: Change): DiffModel {
        val codeReviewManager = applicationService.getProjectService(project).codeReviewManager
        if (null === codeReviewManager) {
            return DiffModelImpl(null, change, listOf(), listOf())
        }
        return DiffModelImpl(codeReviewManager.mergeRequest, change, listOf(), listOf())
    }

    private data class ChangeInfoImpl(
        override val change: Change,
        override val contentRevision: ContentRevision,
        override val before: Boolean,
        override val after: Boolean
    ) : CodeReviewManager.ChangeInfo
}