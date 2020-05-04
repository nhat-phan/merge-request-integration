package net.ntworld.mergeRequestIntegrationIde.diff

import com.intellij.diff.tools.fragmented.UnifiedDiffViewer
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

class UnifiedDiffView(
    private val projectServiceProvider: ProjectServiceProvider,
    override val viewer: UnifiedDiffViewer,
    private val change: Change
) : AbstractDiffView<UnifiedDiffViewer>(projectServiceProvider, viewer) {
    private val myLeftLineNumberConverter by lazy {
        projectServiceProvider.intellijIdeApi.findLeftLineNumberConverter(viewer.editor)
    }
    private val myRightLineNumberConverter by lazy {
        projectServiceProvider.intellijIdeApi.findRightLineNumberConverter(viewer.editor)
    }
    private val myCachedLeftLineNumbers = mutableMapOf<Int, Int>()
    private val myCachedRightLineNumbers = mutableMapOf<Int, Int>()

    override fun convertVisibleLineToLogicalLine(visibleLine: Int, side: Side): Int {
        val map = if (side == Side.LEFT) myCachedLeftLineNumbers else myCachedRightLineNumbers
        val logicalLine = map[visibleLine - 1]
        if (null === logicalLine) {
            return -1
        }
        return logicalLine
    }

    private fun initializeByLogicalLine(reviewContext: ReviewContext, line: Int, side: Side, comments: List<Comment>) {
        initializeThreadOnLineIfNotAvailable(
            reviewContext.providerData, reviewContext.mergeRequestInfo,
            viewer.editor,
            calcPosition(line),
            line, side,
            comments
        )
    }

    override fun initializeLine(reviewContext: ReviewContext, visibleLine: Int, side: Side, comments: List<Comment>) {
        val logicalLine = convertVisibleLineToLogicalLine(visibleLine, side)
        if (-1 != logicalLine) {
            initializeByLogicalLine(reviewContext, logicalLine, side, comments)
            val renderer = findGutterIconRenderer(logicalLine, Side.LEFT)
            if (null !== renderer) {
                updateGutterIcon(renderer, comments)
            }
        }
    }

    override fun prepareLine(reviewContext: ReviewContext, renderer: GutterIconRenderer, comments: List<Comment>) {
        initializeByLogicalLine(reviewContext, renderer.logicalLine, renderer.side, comments)
    }

    override fun createGutterIcons() {
        for (logicalLine in 0 until viewer.editor.document.lineCount) {
            val left = myLeftLineNumberConverter.execute(logicalLine)
            val right = myRightLineNumberConverter.execute(logicalLine)
            myCachedLeftLineNumbers[left] = logicalLine
            myCachedRightLineNumbers[right] = logicalLine

            registerGutterIconRenderer(
                GutterIconRendererFactory.makeGutterIconRenderer(
                    viewer.editor.markupModel.addLineHighlighter(logicalLine, HighlighterLayer.LAST, null),
                    projectServiceProvider.applicationSettings.showAddCommentIconsInDiffViewGutter && (-1 != left || -1 != right),
                    logicalLine,
                    visibleLineLeft = left + 1,
                    visibleLineRight = right + 1,
                    // Doesn't matter, unified view only have 1 side
                    side = Side.LEFT,
                    actionListener = myGutterIconRendererActionListener
                )
            )
        }
    }

    private fun findGutterIconRenderer(visibleLine: Int, side: Side, invoker: ((Int, GutterIconRenderer) -> Unit)) {
        val logicalLine = convertVisibleLineToLogicalLine(visibleLine, side)
        if (-1 == logicalLine) {
            return
        }

        // Doesn't matter, unified view only have 1 side
        // see exact comment above
        val renderer = findGutterIconRenderer(logicalLine, Side.LEFT)
        if (null !== renderer) {
            invoker.invoke(logicalLine, renderer)
        }
    }

    override fun updateComments(visibleLine: Int, side: Side, comments: List<Comment>) {
        findGutterIconRenderer(visibleLine, side) { _, renderer ->
            updateComments(renderer, comments)
        }
    }

    override fun displayComments(renderer: GutterIconRenderer, mode: DiffView.DisplayCommentMode) {
        // for unified view the line distributed either on left/right, so we have to find both of them
        val thread = findThreadPresenter(renderer.logicalLine, renderer.side)
        if (null !== thread) {
            displayComments(thread.model, renderer.logicalLine, renderer.side, mode)
        } else {
            val oppositeSide = if (renderer.side == Side.LEFT) Side.RIGHT else Side.LEFT
            val oppositeThread = findThreadPresenter(renderer.logicalLine, oppositeSide)
            if (null !== oppositeThread) {
                displayComments(oppositeThread.model, renderer.logicalLine, oppositeSide, mode)
            }
        }
    }

    override fun scrollToPosition(position: CommentPosition, showComments: Boolean) {
        if (viewer.editor.isDisposed) {
            return
        }

        val oldLine = position.oldLine
        val newLine = position.newLine
        if (null !== newLine && null === oldLine) {
            return findGutterIconRenderer(newLine, Side.RIGHT) { logicalLine, renderer ->
                viewer.editor.scrollingModel.scrollTo(LogicalPosition(logicalLine, 0), ScrollType.MAKE_VISIBLE)
                if (showComments) {
                    displayComments(renderer, DiffView.DisplayCommentMode.SHOW)
                }
            }
        }

        return findGutterIconRenderer(oldLine!!, Side.LEFT) { logicalLine, renderer ->
            viewer.editor.scrollingModel.scrollTo(LogicalPosition(logicalLine, 0), ScrollType.MAKE_VISIBLE)
            if (showComments) {
                displayComments(renderer, DiffView.DisplayCommentMode.SHOW)
            }
        }
    }

    private fun calcPosition(logicalLine: Int): GutterPosition {
        val left = myLeftLineNumberConverter.execute(logicalLine) + 1
        val right = myRightLineNumberConverter.execute(logicalLine) + 1
        return GutterPosition(
            editorType = DiffView.EditorType.UNIFIED,
            changeType = findChangeType(viewer.editor, logicalLine),
            oldLine = if (left > 0) left else -1,
            oldPath = change.beforeRevision!!.file.toString(),
            newLine = if (right > 0) right else -1,
            newPath = change.afterRevision!!.file.toString(),
            baseHash = change.beforeRevision!!.revisionNumber.asString(),
            headHash = change.afterRevision!!.revisionNumber.asString()
        )
    }
}