package net.ntworld.mergeRequestIntegrationIde.diff

import com.intellij.diff.tools.util.side.TwosideTextDiffViewer
import com.intellij.diff.util.Side
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.editor.markup.HighlighterLayer
import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequestIntegrationIde.diff.gutter.AddGutterIconRenderer
import net.ntworld.mergeRequestIntegrationIde.diff.gutter.CommentsGutterIconRenderer
import net.ntworld.mergeRequestIntegrationIde.service.ApplicationService

class TwoSideTextDiffView(
    private val applicationService: ApplicationService,
    override val viewer: TwosideTextDiffViewer
) : AbstractDiffView<TwosideTextDiffViewer>(viewer) {

    override fun displayAddGutterIcons() {
        println("displayAddGutterIcons")
        for (logicalLine in 0 until viewer.editor1.document.lineCount) {
            val lineHighlighter = viewer.editor1.markupModel.addLineHighlighter(logicalLine, HighlighterLayer.LAST, null)
            lineHighlighter.gutterIconRenderer = AddGutterIconRenderer(
                applicationService.settings.showAddCommentIconsInDiffViewGutter && !hasCommentsGutter(logicalLine, DiffView.ContentType.BEFORE),
                logicalLine + 1,
                logicalLine,
                this::onAddGutterIconInEditor1Clicked
            )
        }
        for (logicalLine in 0 until viewer.editor2.document.lineCount) {
            val lineHighlighter = viewer.editor2.markupModel.addLineHighlighter(logicalLine, HighlighterLayer.LAST, null)
            lineHighlighter.gutterIconRenderer = AddGutterIconRenderer(
                applicationService.settings.showAddCommentIconsInDiffViewGutter && !hasCommentsGutter(logicalLine, DiffView.ContentType.AFTER),
                logicalLine + 1,
                logicalLine,
                this::onAddGutterIconInEditor2Clicked
            )
        }
    }

    private fun onAddGutterIconInEditor1Clicked(renderer: AddGutterIconRenderer, e: AnActionEvent?) {
        val result = viewer.syncScrollSupport!!.scrollable.transfer(Side.LEFT, renderer.visibleLine)
        println("onAddGutterIconInEditor1Clicked old: ${renderer.visibleLine}, new: $result")
    }

    private fun onAddGutterIconInEditor2Clicked(renderer: AddGutterIconRenderer, e: AnActionEvent?) {
        val result = viewer.syncScrollSupport!!.scrollable.transfer(Side.RIGHT, renderer.visibleLine)
        println("onAddGutterIconInEditor2Clicked old: $result, new: ${renderer.visibleLine}")
    }

    override fun displayCommentsGutterIcon(line: Int, contentType: DiffView.ContentType, comments: List<Comment>) {
        val editor = if (contentType == DiffView.ContentType.BEFORE) {
            viewer.editor1
        } else {
            viewer.editor2
        }

        val logicalLine = line - 1
        if (!hasCommentsGutter(logicalLine, contentType)) {
            val lineHighlighter = editor.markupModel.addLineHighlighter(logicalLine, HighlighterLayer.LAST, null)
            lineHighlighter.gutterIconRenderer = CommentsGutterIconRenderer(
                line, logicalLine, dispatcher.multicaster::onCommentsGutterIconClicked
            )
        }
        registerCommentsGutter(logicalLine, contentType, comments)
    }

    override fun hideComments() {
    }
}