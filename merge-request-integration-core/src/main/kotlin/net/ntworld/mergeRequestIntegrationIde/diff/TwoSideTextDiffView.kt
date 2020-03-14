package net.ntworld.mergeRequestIntegrationIde.diff

import com.intellij.diff.tools.util.DiffDataKeys
import com.intellij.diff.tools.util.base.DiffViewerListener
import com.intellij.diff.tools.util.side.TwosideTextDiffViewer
import com.intellij.diff.util.Side
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.editor.markup.HighlighterLayer
import com.intellij.util.EventDispatcher
import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequestIntegrationIde.diff.gutter.AddGutterIconRenderer
import net.ntworld.mergeRequestIntegrationIde.service.ApplicationService

class TwoSideTextDiffView(
    private val applicationService: ApplicationService,
    override val viewer: TwosideTextDiffViewer
) : DiffView<TwosideTextDiffViewer> {
    override val dispatcher = EventDispatcher.create(DiffView.Action::class.java)
    private val diffViewerListener = object : DiffViewerListener() {
        override fun onInit() = dispatcher.multicaster.onInit()
        override fun onDispose() = dispatcher.multicaster.onDispose()
        override fun onBeforeRediff() = dispatcher.multicaster.onBeforeRediff()
        override fun onAfterRediff() = dispatcher.multicaster.onAfterRediff()
        override fun onRediffAborted() = dispatcher.multicaster.onRediffAborted()
    }

    init {
        viewer.addListener(diffViewerListener)
    }

    override fun displayAddGutterIcons() {
        println("displayAddGutterIcons")
        for (line in 0 until viewer.editor1.document.lineCount) {
            val lineHighlighter = viewer.editor1.markupModel.addLineHighlighter(line, HighlighterLayer.LAST, null)
            lineHighlighter.gutterIconRenderer = AddGutterIconRenderer(
                line + 1,
                null,
                null,
                null,
                null,
                this::onAddGutterIconInEditor1Clicked
            )
        }
        for (line in 0 until viewer.editor2.document.lineCount) {
            val lineHighlighter = viewer.editor2.markupModel.addLineHighlighter(line, HighlighterLayer.LAST, null)
            lineHighlighter.gutterIconRenderer = AddGutterIconRenderer(
                line + 1,
                null,
                null,
                null,
                null,
                this::onAddGutterIconInEditor2Clicked
            )
        }
    }

    private fun onAddGutterIconInEditor1Clicked(renderer: AddGutterIconRenderer, e: AnActionEvent) {
        val result = viewer.syncScrollSupport!!.scrollable.transfer(Side.LEFT, renderer.visibleLine)
        println("onAddGutterIconInEditor1Clicked $result")
    }

    private fun onAddGutterIconInEditor2Clicked(renderer: AddGutterIconRenderer, e: AnActionEvent) {
        val result = viewer.syncScrollSupport!!.scrollable.transfer(Side.RIGHT, renderer.visibleLine)
        println("onAddGutterIconInEditor2Clicked $result")
    }

    override fun displayCommentsGutterIcon(line: Int, contentType: DiffView.ContentType, comments: List<Comment>) {
        println("displayCommentsGutterIcon")
    }

    override fun hideComments() {
    }
}