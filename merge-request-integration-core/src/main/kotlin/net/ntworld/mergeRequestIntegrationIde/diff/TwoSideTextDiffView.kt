package net.ntworld.mergeRequestIntegrationIde.diff

import com.intellij.diff.tools.util.side.TwosideTextDiffViewer
import com.intellij.diff.util.Side
import com.intellij.openapi.editor.LogicalPosition
import com.intellij.openapi.editor.ScrollType
import com.intellij.openapi.editor.markup.HighlighterLayer
import com.intellij.openapi.vcs.changes.Change
import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequest.CommentPosition
import net.ntworld.mergeRequestIntegrationIde.diff.gutter.GutterIconRenderer
import net.ntworld.mergeRequestIntegrationIde.diff.gutter.GutterIconRendererFactory
import net.ntworld.mergeRequestIntegrationIde.diff.gutter.GutterPosition
import net.ntworld.mergeRequestIntegrationIde.infrastructure.ProjectServiceProvider
import net.ntworld.mergeRequestIntegrationIde.infrastructure.ReviewContext

class TwoSideTextDiffView(
    private val projectServiceProvider: ProjectServiceProvider,
    override val viewer: TwosideTextDiffViewer,
    private val change: Change
) : AbstractDiffView<TwosideTextDiffViewer>(projectServiceProvider, viewer) {

    override fun convertVisibleLineToLogicalLine(visibleLine: Int, side: Side): Int {
        return visibleLine - 1
    }

    private fun initializeByLogicalLine(reviewContext: ReviewContext, line: Int, side: Side, comments: List<Comment>) {
        if (side == Side.LEFT) {
            initializeThreadOnLineIfNotAvailable(
                reviewContext.providerData, reviewContext.mergeRequestInfo,
                viewer.editor1,
                calcPositionEditor1(line),
                line, side,
                comments
            )
        } else {
            initializeThreadOnLineIfNotAvailable(
                reviewContext.providerData, reviewContext.mergeRequestInfo,
                viewer.editor2,
                calcPositionEditor2(line),
                line, side,
                comments
            )
        }
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
        for (logicalLine in 0 until viewer.editor1.document.lineCount) {
            registerGutterIconRenderer(
                GutterIconRendererFactory.makeGutterIconRenderer(
                    viewer.editor1.markupModel.addLineHighlighter(logicalLine, HighlighterLayer.LAST, null),
                    projectServiceProvider.applicationSettings.showAddCommentIconsInDiffViewGutter,
                    logicalLine,
                    visibleLineLeft = logicalLine + 1,
                    visibleLineRight = null,
                    side = Side.LEFT,
                    action = ::dispatchOnGutterActionPerformed
                )
            )
        }
        for (logicalLine in 0 until viewer.editor2.document.lineCount) {
            registerGutterIconRenderer(
                GutterIconRendererFactory.makeGutterIconRenderer(
                    viewer.editor2.markupModel.addLineHighlighter(logicalLine, HighlighterLayer.LAST, null),
                    projectServiceProvider.applicationSettings.showAddCommentIconsInDiffViewGutter,
                    logicalLine,
                    visibleLineLeft = null,
                    visibleLineRight = logicalLine + 1,
                    side = Side.RIGHT,
                    action = ::dispatchOnGutterActionPerformed
                )
            )
        }
    }

    override fun updateComments(visibleLine: Int, side: Side, comments: List<Comment>) {
        val renderer = findGutterIconRenderer(visibleLine - 1, side)
        if (null !== renderer) {
            updateComments(renderer, comments)
        }
    }

    override fun scrollToPosition(position: CommentPosition, showComments: Boolean) {
        val oldLine = position.oldLine
        if (oldLine !== null && !viewer.editor1.isDisposed) {
            val logicalLine = convertVisibleLineToLogicalLine(oldLine, Side.LEFT)
            viewer.editor1.scrollingModel.scrollTo(LogicalPosition(logicalLine, 0), ScrollType.MAKE_VISIBLE)
            if (showComments) {
                displayComments(oldLine, Side.LEFT, DiffView.DisplayCommentMode.SHOW)
            }
        }

        val newLine = position.newLine
        if (newLine !== null && !viewer.editor1.isDisposed) {
            val logicalLine = convertVisibleLineToLogicalLine(newLine, Side.RIGHT)
            viewer.editor2.scrollingModel.scrollTo(LogicalPosition(logicalLine, 0), ScrollType.MAKE_VISIBLE)
            if (showComments) {
                displayComments(newLine, Side.RIGHT, DiffView.DisplayCommentMode.SHOW)
            }
        }
    }

    private fun calcPositionEditor1(logicalLine: Int): GutterPosition {
        val newLine = viewer.syncScrollSupport!!.scrollable.transfer(Side.LEFT, logicalLine + 1)
        return GutterPosition(
            editorType = DiffView.EditorType.TWO_SIDE_LEFT,
            changeType = findChangeType(viewer.editor1, logicalLine),
            oldLine = logicalLine + 1,
            oldPath = change.beforeRevision!!.file.toString(),
            newLine = newLine,
            newPath = change.afterRevision!!.file.toString(),
            baseHash = change.beforeRevision!!.revisionNumber.asString(),
            headHash = change.afterRevision!!.revisionNumber.asString()
        )
    }

    private fun calcPositionEditor2(logicalLine: Int): GutterPosition {
        val oldLine = viewer.syncScrollSupport!!.scrollable.transfer(Side.RIGHT, logicalLine + 1)
        return GutterPosition(
            editorType = DiffView.EditorType.TWO_SIDE_RIGHT,
            changeType = findChangeType(viewer.editor2, logicalLine),
            oldLine = oldLine,
            oldPath = change.beforeRevision!!.file.toString(),
            newLine = logicalLine + 1,
            newPath = change.afterRevision!!.file.toString(),
            baseHash = change.beforeRevision!!.revisionNumber.asString(),
            headHash = change.afterRevision!!.revisionNumber.asString()
        )
    }
}