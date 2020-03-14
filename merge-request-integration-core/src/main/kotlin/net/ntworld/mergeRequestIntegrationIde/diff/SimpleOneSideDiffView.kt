package net.ntworld.mergeRequestIntegrationIde.diff

import com.intellij.diff.tools.simple.SimpleOnesideDiffViewer
import com.intellij.diff.tools.util.base.DiffViewerListener
import com.intellij.openapi.editor.markup.HighlighterLayer
import com.intellij.util.EventDispatcher
import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequestIntegrationIde.diff.gutter.AddGutterIconRenderer
import net.ntworld.mergeRequestIntegrationIde.diff.gutter.CommentsGutterIconRenderer
import net.ntworld.mergeRequestIntegrationIde.service.ApplicationService

class SimpleOneSideDiffView(
    private val applicationService: ApplicationService,
    override val viewer: SimpleOnesideDiffViewer
) : DiffViewBase<SimpleOnesideDiffViewer>(viewer) {
    override fun displayAddGutterIcons() {
        for (line in 0 until viewer.editor.document.lineCount) {
            val lineHighlighter = viewer.editor.markupModel.addLineHighlighter(line, HighlighterLayer.LAST, null)
            lineHighlighter.gutterIconRenderer = AddGutterIconRenderer(
                applicationService.settings.showAddCommentIconsInDiffViewGutter,
                line + 1,
                dispatcher.multicaster::onAddGutterIconClicked
            )
        }
    }

    override fun displayCommentsGutterIcon(line: Int, contentType: DiffView.ContentType, comments: List<Comment>) {
        val lineHighlighter = viewer.editor.markupModel.addLineHighlighter(line - 1, HighlighterLayer.LAST, null)
        lineHighlighter.gutterIconRenderer = CommentsGutterIconRenderer(
            line, comments, dispatcher.multicaster::onCommentsGutterIconClicked
        )
    }

    override fun hideComments() {
    }
}