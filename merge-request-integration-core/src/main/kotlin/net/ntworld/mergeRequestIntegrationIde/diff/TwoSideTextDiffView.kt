package net.ntworld.mergeRequestIntegrationIde.diff

import com.intellij.diff.tools.util.side.TwosideTextDiffViewer
import com.intellij.diff.util.Side
import com.intellij.openapi.editor.markup.HighlighterLayer
import com.intellij.openapi.vcs.changes.Change
import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequest.MergeRequest
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequestIntegrationIde.diff.gutter.*
import net.ntworld.mergeRequestIntegrationIde.service.ApplicationService

class TwoSideTextDiffView(
    private val applicationService: ApplicationService,
    override val viewer: TwosideTextDiffViewer,
    private val change: Change
) : AbstractDiffView<TwosideTextDiffViewer>(applicationService, viewer) {

    override fun createGutterIcons() {
        for (logicalLine in 0 until viewer.editor1.document.lineCount) {
            registerGutterIconRenderer(GutterIconRendererFactory.makeGutterIconRenderer(
                viewer.editor1.markupModel.addLineHighlighter(logicalLine, HighlighterLayer.LAST, null),
                applicationService.settings.showAddCommentIconsInDiffViewGutter,
                logicalLine,
                visibleLine = logicalLine + 1,
                contentType = DiffView.ContentType.BEFORE,
                action = this::onGutterIconActionTriggered
            ))
        }
        for (logicalLine in 0 until viewer.editor2.document.lineCount) {
            registerGutterIconRenderer(GutterIconRendererFactory.makeGutterIconRenderer(
                viewer.editor2.markupModel.addLineHighlighter(logicalLine, HighlighterLayer.LAST, null),
                applicationService.settings.showAddCommentIconsInDiffViewGutter,
                logicalLine,
                visibleLine = logicalLine + 1,
                contentType = DiffView.ContentType.AFTER,
                action = this::onGutterIconActionTriggered
            ))
        }
    }

    private fun onGutterIconActionTriggered(renderer: GutterIconRenderer, actionType: GutterActionType) {
        when (actionType) {
            GutterActionType.ADD -> {
                val position = if (renderer.contentType == DiffView.ContentType.BEFORE)
                    calcPositionEditor1(renderer.logicalLine)
                else
                    calcPositionEditor2(renderer.logicalLine)
                dispatcher.multicaster.onAddGutterIconClicked(renderer, position)
            }
            GutterActionType.TOGGLE -> {
                dispatcher.multicaster.onCommentsGutterIconClicked(renderer)
            }
        }
    }

    override fun changeGutterIconsByComments(visibleLine: Int, contentType: DiffView.ContentType, comments: List<Comment>) {
        val editor = if (contentType == DiffView.ContentType.BEFORE) {
            viewer.editor1
        } else {
            viewer.editor2
        }

        val logicalLine = visibleLine - 1
        if (!hasCommentsGutter(logicalLine, contentType)) {
            val lineHighlighter = editor.markupModel.addLineHighlighter(logicalLine, HighlighterLayer.LAST, null)
            lineHighlighter.gutterIconRenderer = CommentsGutterIconRenderer(
                visibleLine, logicalLine, contentType, dispatcher.multicaster::legacyOnCommentsGutterIconClicked
            )
        }
        registerCommentsGutter(logicalLine, contentType, comments)

        val gutterIconRenderer = findGutterIconRenderer(logicalLine, contentType)
        gutterIconRenderer.setState(GutterState.COMMENTS_FROM_ONE_AUTHOR)
    }

    override fun toggleCommentsOnLine(
        providerData: ProviderData,
        mergeRequest: MergeRequest,
        visibleLine: Int,
        logicalLine: Int,
        contentType: DiffView.ContentType,
        comments: List<Comment>
    ) {
        if (contentType == DiffView.ContentType.BEFORE) {
            toggleCommentsOnLine(
                providerData, mergeRequest, viewer.editor1, calcPositionEditor1(logicalLine), logicalLine, contentType, comments
            )
        } else {
            toggleCommentsOnLine(
                providerData, mergeRequest, viewer.editor2, calcPositionEditor2(logicalLine), logicalLine, contentType, comments
            )
        }
    }

    private fun calcPositionEditor1(logicalLine: Int): AddCommentRequestedPosition {
        val newLine = viewer.syncScrollSupport!!.scrollable.transfer(Side.LEFT, logicalLine + 1)
        return AddCommentRequestedPosition(
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

    private fun calcPositionEditor2(logicalLine: Int): AddCommentRequestedPosition {
        val oldLine = viewer.syncScrollSupport!!.scrollable.transfer(Side.RIGHT, logicalLine + 1)
        return AddCommentRequestedPosition(
            editorType = DiffView.EditorType.TWO_SIDE_RIGHT,
            changeType = findChangeType(viewer.editor1, logicalLine),
            oldLine = oldLine,
            oldPath = change.beforeRevision!!.file.toString(),
            newLine = logicalLine + 1,
            newPath = change.afterRevision!!.file.toString(),
            baseHash = change.beforeRevision!!.revisionNumber.asString(),
            headHash = change.afterRevision!!.revisionNumber.asString()
        )
    }
}