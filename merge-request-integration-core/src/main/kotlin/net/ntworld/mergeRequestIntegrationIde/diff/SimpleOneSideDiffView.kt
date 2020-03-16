package net.ntworld.mergeRequestIntegrationIde.diff

import com.intellij.diff.tools.simple.SimpleOnesideDiffViewer
import com.intellij.openapi.editor.markup.HighlighterLayer
import com.intellij.openapi.vcs.changes.Change
import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequestIntegrationIde.diff.gutter.AddGutterIconRenderer
import net.ntworld.mergeRequestIntegrationIde.diff.gutter.CommentsGutterIconRenderer
import net.ntworld.mergeRequestIntegrationIde.service.ApplicationService

class SimpleOneSideDiffView(
    private val applicationService: ApplicationService,
    override val viewer: SimpleOnesideDiffViewer,
    private val change: Change,
    private val contentType: DiffView.ContentType
) : AbstractDiffView<SimpleOnesideDiffViewer>(applicationService, viewer) {
    override fun displayAddGutterIcons() {
        for (logicalLine in 0 until viewer.editor.document.lineCount) {
            val lineHighlighter = viewer.editor.markupModel.addLineHighlighter(logicalLine, HighlighterLayer.LAST, null)

            lineHighlighter.gutterIconRenderer = AddGutterIconRenderer(
                applicationService.settings.showAddCommentIconsInDiffViewGutter && !hasCommentsGutter(
                    logicalLine,
                    contentType
                ),
                logicalLine + 1,
                logicalLine,
                this::onAddGutterIconClicked
            )
        }
    }

    private fun onAddGutterIconClicked(renderer: AddGutterIconRenderer, changeType: DiffView.ChangeType?) {
        val position = calcPosition(
            renderer.visibleLine,
            if (null !== changeType) changeType else findChangeType(viewer.editor, renderer.logicalLine)
        )
        dispatcher.multicaster.onAddGutterIconClicked(renderer, position)
    }

    override fun displayCommentsGutterIcon(
        visibleLine: Int,
        contentType: DiffView.ContentType,
        comments: List<Comment>
    ) {
        val logicalLine = visibleLine - 1
        if (!hasCommentsGutter(logicalLine, contentType)) {
            val lineHighlighter = viewer.editor.markupModel.addLineHighlighter(logicalLine, HighlighterLayer.LAST, null)
            lineHighlighter.gutterIconRenderer = CommentsGutterIconRenderer(
                visibleLine, logicalLine, contentType, dispatcher.multicaster::onCommentsGutterIconClicked
            )
        }
        registerCommentsGutter(logicalLine, contentType, comments)
    }

    override fun displayCommentsOnLine(
        providerData: ProviderData,
        visibleLine: Int,
        logicalLine: Int,
        contentType: DiffView.ContentType,
        comments: List<Comment>
    ) {
        toggleCommentsOnLine(
            providerData,
            viewer.editor,
            calcPosition(visibleLine, findChangeType(viewer.editor, logicalLine)),
            logicalLine,
            contentType,
            comments
        )
    }

    private fun calcPosition(visibleLine: Int, changeType: DiffView.ChangeType): AddCommentRequestedPosition {
        return if (contentType == DiffView.ContentType.BEFORE) {
            AddCommentRequestedPosition(
                editorType = DiffView.EditorType.SINGLE_SIDE,
                changeType = changeType,
                oldLine = visibleLine,
                oldPath = change.beforeRevision!!.file.toString(),
                newLine = null,
                newPath = null,
                baseHash = change.beforeRevision!!.revisionNumber.asString()
            )
        } else {
            AddCommentRequestedPosition(
                editorType = DiffView.EditorType.SINGLE_SIDE,
                changeType = changeType,
                newLine = visibleLine,
                newPath = change.afterRevision!!.file.toString(),
                oldLine = null,
                oldPath = null,
                headHash = change.afterRevision!!.revisionNumber.asString()
            )
        }
    }
}