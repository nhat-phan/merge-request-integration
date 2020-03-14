package net.ntworld.mergeRequestIntegrationIde.diff

import com.intellij.diff.tools.fragmented.UnifiedDiffViewer
import com.intellij.diff.tools.util.base.DiffViewerListener
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.editor.EditorGutter
import com.intellij.openapi.editor.markup.HighlighterLayer
import com.intellij.util.EventDispatcher
import gnu.trove.TIntFunction
import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequestIntegrationIde.diff.gutter.AddGutterIconRenderer
import net.ntworld.mergeRequestIntegrationIde.service.ApplicationService
import java.lang.reflect.Field

class UnifiedDiffView(
    private val applicationService: ApplicationService,
    override val viewer: UnifiedDiffViewer
) : DiffView<UnifiedDiffViewer> {
    override val dispatcher = EventDispatcher.create(DiffView.Action::class.java)
    private val diffViewerListener = object : DiffViewerListener() {
        override fun onInit() = dispatcher.multicaster.onInit()
        override fun onDispose() = dispatcher.multicaster.onDispose()
        override fun onBeforeRediff() = dispatcher.multicaster.onBeforeRediff()
        override fun onAfterRediff() = dispatcher.multicaster.onAfterRediff()
        override fun onRediffAborted() = dispatcher.multicaster.onRediffAborted()
    }
    private val leftLineNumberConverter by lazy {
        try {
            val myLineNumberConvertor = viewer.editor.gutter.javaClass.getDeclaredField("myLineNumberConvertor")
            myLineNumberConvertor.isAccessible = true
            myLineNumberConvertor.get(viewer.editor.gutter) as TIntFunction
        } catch (exception: NoSuchFieldException) {
            val myLineNumberConverter = viewer.editor.gutter.javaClass.getDeclaredField("myLineNumberConverter")
            myLineNumberConverter.isAccessible = true
            myLineNumberConverter.get(viewer.editor.gutter) as TIntFunction
        }
    }
    private val rightLineNumberConverter by lazy {
        try {
            val myAdditionalLineNumberConvertor = viewer.editor.gutter.javaClass.getDeclaredField(
                "myAdditionalLineNumberConvertor"
            )
            myAdditionalLineNumberConvertor.isAccessible = true
            myAdditionalLineNumberConvertor.get(viewer.editor.gutter) as TIntFunction
        } catch (exception: NoSuchFieldException) {
            val myAdditionalLineNumberConverter = viewer.editor.gutter.javaClass.getDeclaredField(
                "myAdditionalLineNumberConverter"
            )
            myAdditionalLineNumberConverter.isAccessible = true
            myAdditionalLineNumberConverter.get(viewer.editor.gutter) as TIntFunction
        }
    }

    init {
        viewer.addListener(diffViewerListener)
    }

    override fun displayAddGutterIcons() {
        for (line in 0 until viewer.editor.document.lineCount) {
            val left = leftLineNumberConverter.execute(line)
            val right = rightLineNumberConverter.execute(line)
            if (-1 == left && -1 == right) {
                continue
            }

            val lineHighlighter = viewer.editor.markupModel.addLineHighlighter(line, HighlighterLayer.LAST, null)
            lineHighlighter.gutterIconRenderer = AddGutterIconRenderer(
                line + 1,
                null,
                null,
                null,
                null,
                this::onAddGutterIconClicked
            )
        }
    }

    private fun onAddGutterIconClicked(renderer: AddGutterIconRenderer, e: AnActionEvent) {
        println("onAddGutterIconClicked left: ${leftLineNumberConverter.execute(renderer.visibleLine)}, right: ${rightLineNumberConverter.execute(renderer.visibleLine)}")
    }

    override fun displayCommentsGutterIcon(line: Int, contentType: DiffView.ContentType, comments: List<Comment>) {
    }

    override fun hideComments() {
    }
}