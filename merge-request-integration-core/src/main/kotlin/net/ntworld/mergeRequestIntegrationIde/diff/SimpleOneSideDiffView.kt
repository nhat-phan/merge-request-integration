package net.ntworld.mergeRequestIntegrationIde.diff

import com.intellij.diff.tools.simple.SimpleOnesideDiffViewer
import com.intellij.diff.util.Side
import com.intellij.openapi.editor.LogicalPosition
import com.intellij.openapi.editor.ScrollType
import com.intellij.openapi.editor.markup.HighlighterLayer
import com.intellij.openapi.vcs.changes.Change
import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequest.CommentPosition
import net.ntworld.mergeRequestIntegrationIde.component.gutter.GutterIconRenderer
import net.ntworld.mergeRequestIntegrationIde.component.gutter.GutterIconRendererFactory
import net.ntworld.mergeRequestIntegrationIde.component.gutter.GutterPosition
import net.ntworld.mergeRequestIntegrationIde.infrastructure.ProjectServiceProvider
import net.ntworld.mergeRequestIntegrationIde.infrastructure.ReviewContext

class SimpleOneSideDiffView(
    private val projectServiceProvider: ProjectServiceProvider,
    override val viewer: SimpleOnesideDiffViewer,
    private val change: Change,
    private val side: Side
) : AbstractDiffView<SimpleOnesideDiffViewer>(projectServiceProvider, viewer) {

    override fun convertVisibleLineToLogicalLine(visibleLine: Int, side: Side): Int {
        return visibleLine - 1
    }

    private fun initializeByLogicalLine(reviewContext: ReviewContext, line: Int, side: Side, comments: List<Comment>) {
        initializeThreadOnLineIfNotAvailable(
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
                projectServiceProvider.applicationSettings.showAddCommentIconsInDiffViewGutter,
                logicalLine,
                visibleLineLeft = if (side == Side.LEFT) logicalLine + 1 else null,
                visibleLineRight = if (side == Side.RIGHT) logicalLine + 1 else null,
                side = side,
                actionListener = myGutterIconRendererActionListener
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

    override fun scrollToPosition(position: CommentPosition, showComments: Boolean) {
        if (viewer.editor.isDisposed) {
            return
        }

        val oldLine = position.oldLine
        if (side == Side.LEFT && null !== oldLine) {
            val logicalLine = convertVisibleLineToLogicalLine(oldLine, Side.LEFT)
            viewer.editor.scrollingModel.scrollTo(LogicalPosition(logicalLine, 0), ScrollType.MAKE_VISIBLE)
            if (showComments) {
                displayComments(oldLine, Side.LEFT, DiffView.DisplayCommentMode.SHOW)
            }
            return
        }

        val newLine = position.newLine
        if (side == Side.RIGHT && null !== newLine) {
            val logicalLine = convertVisibleLineToLogicalLine(newLine, Side.RIGHT)
            viewer.editor.scrollingModel.scrollTo(LogicalPosition(logicalLine, 0), ScrollType.MAKE_VISIBLE)
            if (showComments) {
                displayComments(newLine, Side.RIGHT, DiffView.DisplayCommentMode.SHOW)
            }
            return
        }
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