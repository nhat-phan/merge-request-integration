package net.ntworld.mergeRequestIntegrationIde.diff

import com.intellij.diff.tools.simple.SimpleOnesideDiffViewer
import com.intellij.openapi.editor.markup.HighlighterLayer
import com.intellij.openapi.vcs.changes.Change
import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequest.MergeRequest
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequestIntegrationIde.diff.gutter.*
import net.ntworld.mergeRequestIntegrationIde.service.ApplicationService

class SimpleOneSideDiffView(
    private val applicationService: ApplicationService,
    override val viewer: SimpleOnesideDiffViewer,
    private val change: Change,
    private val contentType: DiffView.ContentType
) : AbstractDiffView<SimpleOnesideDiffViewer>(applicationService, viewer) {

    override fun createGutterIcons() {
        for (logicalLine in 0 until viewer.editor.document.lineCount) {
            registerGutterIconRenderer(GutterIconRendererFactory.makeGutterIconRenderer(
                viewer.editor.markupModel.addLineHighlighter(logicalLine, HighlighterLayer.LAST, null),
                applicationService.settings.showAddCommentIconsInDiffViewGutter,
                logicalLine,
                visibleLine = logicalLine + 1,
                contentType = contentType,
                action = this::onGutterIconActionTriggered
            ))
        }
    }

    private fun onGutterIconActionTriggered(renderer: GutterIconRenderer, actionType: GutterActionType) {
        when (actionType) {
            GutterActionType.ADD -> {
                val position = calcPosition(renderer.logicalLine)
                dispatcher.multicaster.onAddGutterIconClicked(renderer, position)
            }
            GutterActionType.TOGGLE -> {
                dispatcher.multicaster.onCommentsGutterIconClicked(renderer)
            }
        }
    }

    override fun changeGutterIconsByComments(
        visibleLine: Int,
        contentType: DiffView.ContentType,
        comments: List<Comment>
    ) {
        val logicalLine = visibleLine - 1
        if (!hasCommentsGutter(logicalLine, contentType)) {
            val lineHighlighter = viewer.editor.markupModel.addLineHighlighter(logicalLine, HighlighterLayer.LAST, null)
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
        toggleCommentsOnLine(
            providerData,
            mergeRequest,
            viewer.editor,
            calcPosition(logicalLine),
            logicalLine,
            contentType,
            comments
        )
    }

    private fun calcPosition(logicalLine: Int): AddCommentRequestedPosition {
        return if (contentType == DiffView.ContentType.BEFORE) {
            AddCommentRequestedPosition(
                editorType = DiffView.EditorType.SINGLE_SIDE,
                changeType = findChangeType(viewer.editor, logicalLine),
                oldLine = logicalLine + 1,
                oldPath = change.beforeRevision!!.file.toString(),
                newLine = null,
                newPath = null,
                baseHash = change.beforeRevision!!.revisionNumber.asString()
            )
        } else {
            AddCommentRequestedPosition(
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