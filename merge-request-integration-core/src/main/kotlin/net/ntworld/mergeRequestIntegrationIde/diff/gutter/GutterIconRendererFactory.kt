package net.ntworld.mergeRequestIntegrationIde.diff.gutter

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.markup.RangeHighlighter
import net.ntworld.mergeRequestIntegrationIde.diff.DiffView

object GutterIconRendererFactory {

    fun makeGutterIconRenderer(
        highlighter: RangeHighlighter,
        showAddIcon: Boolean,
        logicalLine: Int,
        visibleLineLeft: Int?,
        visibleLineRight: Int?,
        contentType: DiffView.ContentType,
        action: ((GutterIconRenderer, GutterActionType) -> Unit)
    ): GutterIconRenderer {
        val gutterIconRenderer = GutterIconRendererImpl(
            showAddIcon, visibleLineLeft, visibleLineRight, logicalLine, contentType, action
        )
        highlighter.gutterIconRenderer = gutterIconRenderer
        return gutterIconRenderer
    }

    fun findGutterIconRenderer(editor: Editor): GutterIconRenderer? {
        val logicalLine = editor.caretModel.logicalPosition.line
        for (highlighter in editor.markupModel.allHighlighters) {
            val gutterRenderer = highlighter.gutterIconRenderer
            if (gutterRenderer !is GutterIconRenderer || gutterRenderer.logicalLine != logicalLine) {
                continue
            }
            return gutterRenderer
        }
        return null
    }
}