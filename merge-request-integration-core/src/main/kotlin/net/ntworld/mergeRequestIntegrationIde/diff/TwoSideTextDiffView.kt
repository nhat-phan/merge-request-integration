package net.ntworld.mergeRequestIntegrationIde.diff

import com.intellij.diff.tools.util.side.TwosideTextDiffViewer
import com.intellij.diff.util.Side
import com.intellij.openapi.application.ApplicationManager
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

    override fun convertVisibleLineToLogicalLine(visibleLine: Int, contentType: DiffView.ContentType): Int {
        return visibleLine - 1
    }

    override fun createGutterIcons() {
        for (logicalLine in 0 until viewer.editor1.document.lineCount) {
            registerGutterIconRenderer(
                GutterIconRendererFactory.makeGutterIconRenderer(
                    viewer.editor1.markupModel.addLineHighlighter(logicalLine, HighlighterLayer.LAST, null),
                    applicationService.settings.showAddCommentIconsInDiffViewGutter,
                    logicalLine,
                    visibleLineLeft = logicalLine + 1,
                    visibleLineRight = null,
                    contentType = DiffView.ContentType.BEFORE,
                    action = ::dispatchOnGutterActionPerformed
                )
            )
        }
        for (logicalLine in 0 until viewer.editor2.document.lineCount) {
            registerGutterIconRenderer(
                GutterIconRendererFactory.makeGutterIconRenderer(
                    viewer.editor2.markupModel.addLineHighlighter(logicalLine, HighlighterLayer.LAST, null),
                    applicationService.settings.showAddCommentIconsInDiffViewGutter,
                    logicalLine,
                    visibleLineLeft = null,
                    visibleLineRight = logicalLine + 1,
                    contentType = DiffView.ContentType.AFTER,
                    action = ::dispatchOnGutterActionPerformed
                )
            )
        }
    }

    override fun changeGutterIconsByComments(
        visibleLine: Int,
        contentType: DiffView.ContentType,
        comments: List<Comment>
    ) {
        updateGutterIcon(findGutterIconRenderer(visibleLine - 1, contentType), comments)
    }

    override fun updateComments(
        providerData: ProviderData,
        mergeRequest: MergeRequest,
        visibleLine: Int,
        contentType: DiffView.ContentType,
        comments: List<Comment>,
        requestSource: DiffModel.Source
    ) {
        if (contentType == DiffView.ContentType.BEFORE) {
            updateComments(
                providerData, mergeRequest, viewer.editor1, calcPositionEditor1(visibleLine - 1),
                findGutterIconRenderer(visibleLine - 1, contentType), comments
            )
        } else {
            updateComments(
                providerData, mergeRequest, viewer.editor2, calcPositionEditor1(visibleLine - 1),
                findGutterIconRenderer(visibleLine - 1, contentType), comments
            )
        }
    }

    override fun displayEditorOnLine(
        providerData: ProviderData,
        mergeRequest: MergeRequest,
        logicalLine: Int,
        contentType: DiffView.ContentType,
        comments: List<Comment>
    ) {
        if (contentType == DiffView.ContentType.BEFORE) {
            displayCommentsAndEditorOnLine(
                providerData, mergeRequest, viewer.editor1, calcPositionEditor1(logicalLine),
                logicalLine, contentType, comments
            )
        } else {
            displayCommentsAndEditorOnLine(
                providerData, mergeRequest, viewer.editor2, calcPositionEditor2(logicalLine),
                logicalLine, contentType, comments
            )
        }
    }

    override fun changeCommentsVisibilityOnLine(
        providerData: ProviderData,
        mergeRequest: MergeRequest,
        logicalLine: Int,
        contentType: DiffView.ContentType,
        comments: List<Comment>,
        mode: DiffView.DisplayCommentMode
    ) {
        if (contentType == DiffView.ContentType.BEFORE) {
            toggleCommentsOnLine(
                providerData, mergeRequest, viewer.editor1, calcPositionEditor1(logicalLine),
                logicalLine, contentType, comments, mode
            )
        } else {
            toggleCommentsOnLine(
                providerData, mergeRequest, viewer.editor2, calcPositionEditor2(logicalLine),
                logicalLine, contentType, comments, mode
            )
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