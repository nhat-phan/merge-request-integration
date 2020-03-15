package net.ntworld.mergeRequestIntegrationIde.diff

import com.intellij.diff.tools.simple.SimpleOnesideDiffViewer
import com.intellij.openapi.editor.markup.HighlighterLayer
import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequestIntegrationIde.diff.gutter.AddGutterIconRenderer
import net.ntworld.mergeRequestIntegrationIde.diff.gutter.CommentsGutterIconRenderer
import net.ntworld.mergeRequestIntegrationIde.service.ApplicationService

class SimpleOneSideDiffView(
    private val applicationService: ApplicationService,
    override val viewer: SimpleOnesideDiffViewer
) : AbstractDiffView<SimpleOnesideDiffViewer>(viewer) {
    override fun displayAddGutterIcons() {
        for (logicalLine in 0 until viewer.editor.document.lineCount) {
            val lineHighlighter = viewer.editor.markupModel.addLineHighlighter(logicalLine, HighlighterLayer.LAST, null)
            val shouldShowIcon = applicationService.settings.showAddCommentIconsInDiffViewGutter && (
                !hasCommentsGutter(logicalLine, DiffView.ContentType.BEFORE) &&
                !hasCommentsGutter(logicalLine, DiffView.ContentType.AFTER)
            )

            lineHighlighter.gutterIconRenderer = AddGutterIconRenderer(
                shouldShowIcon,
                logicalLine + 1,
                logicalLine,
                dispatcher.multicaster::onAddGutterIconClicked
            )
        }
    }

    override fun displayCommentsGutterIcon(line: Int, contentType: DiffView.ContentType, comments: List<Comment>) {
        val logicalLine = line - 1
        if (!hasCommentsGutter(logicalLine, contentType)) {
            val lineHighlighter = viewer.editor.markupModel.addLineHighlighter(logicalLine, HighlighterLayer.LAST, null)
            lineHighlighter.gutterIconRenderer = CommentsGutterIconRenderer(
                line, logicalLine, dispatcher.multicaster::onCommentsGutterIconClicked
            )
        }
        registerCommentsGutter(logicalLine, contentType, comments)
    }

    override fun hideComments() {
    }
}