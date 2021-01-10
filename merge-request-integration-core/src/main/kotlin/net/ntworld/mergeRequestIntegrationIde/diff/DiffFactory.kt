package net.ntworld.mergeRequestIntegrationIde.diff

import com.intellij.diff.tools.fragmented.UnifiedDiffViewer
import com.intellij.diff.tools.simple.SimpleOnesideDiffViewer
import com.intellij.diff.tools.util.base.DiffViewerBase
import com.intellij.diff.tools.util.side.TwosideTextDiffViewer
import com.intellij.diff.util.Side
import com.intellij.openapi.vcs.changes.Change
import net.ntworld.mergeRequestIntegrationIde.infrastructure.ProjectServiceProvider
import net.ntworld.mergeRequestIntegrationIde.infrastructure.ReviewContext

object DiffFactory {
    fun makeDiffPresenter(
        projectServiceProvider: ProjectServiceProvider, model: DiffModel, view: DiffView<*>
    ): DiffPresenter {
        return DiffPresenterImpl(projectServiceProvider, model, view)
    }

    fun makeView(
        projectServiceProvider: ProjectServiceProvider,
        viewer: DiffViewerBase,
        change: Change
    ): DiffView<*>? {
        return when (viewer) {
            is SimpleOnesideDiffViewer -> makeSimpleOneSideDiffViewer(projectServiceProvider, viewer, change)
            is TwosideTextDiffViewer -> makeTwoSideTextDiffViewer(projectServiceProvider, viewer, change)
            is UnifiedDiffViewer -> makeUnifiedDiffView(projectServiceProvider, viewer, change)
            else -> null
        }
    }

    private fun makeSimpleOneSideDiffViewer(
        projectServiceProvider: ProjectServiceProvider, viewer: SimpleOnesideDiffViewer, change: Change
    ): DiffView<SimpleOnesideDiffViewer>? {
        return SimpleOneSideDiffView(projectServiceProvider, viewer, change, when(change.type) {
            Change.Type.DELETED -> Side.LEFT
            Change.Type.NEW -> Side.RIGHT
            else -> throw Exception("Invalid change type")
        })
    }

    private fun makeTwoSideTextDiffViewer(
        projectServiceProvider: ProjectServiceProvider, viewer: TwosideTextDiffViewer, change: Change
    ): DiffView<TwosideTextDiffViewer>? {
        return TwoSideTextDiffView(projectServiceProvider, viewer, change)
    }

    private fun makeUnifiedDiffView(
        projectServiceProvider: ProjectServiceProvider, viewer: UnifiedDiffViewer, change: Change
    ): DiffView<UnifiedDiffViewer>? {
        return UnifiedDiffView(projectServiceProvider, viewer, change)
    }

    fun makeDiffModel(projectServiceProvider: ProjectServiceProvider, reviewContext: ReviewContext, change: Change): DiffModel? {
        return DiffModelImpl(projectServiceProvider, reviewContext, change, false, onlyShowDraftComments = false)
    }
}