package net.ntworld.mergeRequestIntegrationIde.diff

import com.intellij.diff.tools.simple.SimpleOnesideDiffViewer
import com.intellij.diff.util.Side
import com.intellij.openapi.editor.LogicalPosition
import com.intellij.openapi.editor.ScrollType
import com.intellij.openapi.editor.markup.HighlighterLayer
import com.intellij.openapi.vcs.changes.Change
import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequest.MergeRequestInfo
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequestIntegrationIde.diff.gutter.*
import net.ntworld.mergeRequestIntegrationIde.infrastructure.ReviewContext
import net.ntworld.mergeRequestIntegrationIde.service.ApplicationService

class SimpleOneSideDiffView(
    private val applicationService: ApplicationService,
    override val viewer: SimpleOnesideDiffViewer,
    private val change: Change,
    private val side: Side
) : AbstractDiffView<SimpleOnesideDiffViewer>(applicationService, viewer) {

    override fun convertVisibleLineToLogicalLine(visibleLine: Int, side: Side): Int {
        return visibleLine - 1
    }

    private fun initializeByLogicalLine(reviewContext: ReviewContext, line: Int, side: Side, comments: List<Comment>) {
        initializeThreadModelOnLineIfNotAvailable(
            reviewContext.providerData, reviewContext.mergeRequestInfo,
            viewer.editor,
            calcPosition(line),
            line,
            side,
            comments
        )
    }

    override fun initializeLine(reviewContext: ReviewContext, visibleLine: Int, side: Side, comments: List<Comment>) {
        val logicalLine = convertVisibleLineToLogicalLine(visibleLine, side)
        initializeByLogicalLine(reviewContext, logicalLine, side, comments)
        val renderer = findGutterIconRenderer(logicalLine, side)
        if (null !== renderer) {
            updateGutterIcon(renderer, comments)
        }
    }

    override fun prepareLine(reviewContext: ReviewContext, renderer: GutterIconRenderer, comments: List<Comment>) {
        initializeByLogicalLine(reviewContext, renderer.logicalLine, renderer.side, comments)
    }

    override fun createGutterIcons() {
        for (logicalLine in 0 until viewer.editor.document.lineCount) {
            registerGutterIconRenderer(GutterIconRendererFactory.makeGutterIconRenderer(
                viewer.editor.markupModel.addLineHighlighter(logicalLine, HighlighterLayer.LAST, null),
                applicationService.settings.showAddCommentIconsInDiffViewGutter,
                logicalLine,
                visibleLineLeft = if (side == Side.LEFT) logicalLine + 1 else null,
                visibleLineRight = if (side == Side.RIGHT) logicalLine + 1 else null,
                side = side,
                action = ::dispatchOnGutterActionPerformed
            ))
        }
    }

    override fun updateComments(visibleLine: Int, side: Side, comments: List<Comment>) {
        val logicalLine = convertVisibleLineToLogicalLine(visibleLine, side)
        val renderer = findGutterIconRenderer(logicalLine, side)
        if (null !== renderer) {
            updateComments(renderer, comments)
        }
    }

    override fun scrollToLine(visibleLine: Int, side: Side) {
        val logicalLine = convertVisibleLineToLogicalLine(visibleLine, side)
        viewer.editor.scrollingModel.scrollTo(LogicalPosition(logicalLine, 0), ScrollType.MAKE_VISIBLE)
    }

    private fun calcPosition(logicalLine: Int): GutterPosition {
        return if (side == Side.LEFT) {
            GutterPosition(
                editorType = DiffView.EditorType.SINGLE_SIDE,
                changeType = findChangeType(viewer.editor, logicalLine),
                oldLine = logicalLine + 1,
                oldPath = change.beforeRevision!!.file.toString(),
                newLine = null,
                newPath = null,
                baseHash = change.beforeRevision!!.revisionNumber.asString()
            )
        } else {
            GutterPosition(
                editorType = DiffView.EditorType.SINGLE_SIDE,
                changeType = findChangeType(viewer.editor, logicalLine),
                newLine = logicalLine + 1,
                newPath = change.afterRevision!!.file.toString(),
                oldLine = null,
                oldPath = null,
                headHash = change.afterRevision!!.revisionNumber.asString()
            )
        }
    }
}