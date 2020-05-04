package net.ntworld.mergeRequestIntegrationIde.component.gutter

import com.intellij.diff.util.Side
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.markup.RangeHighlighter

object GutterIconRendererFactory {

    fun makeGutterIconRenderer(
        highlighter: RangeHighlighter,
        showAddIcon: Boolean,
        logicalLine: Int,
        visibleLineLeft: Int?,
        visibleLineRight: Int?,
        side: Side,
        actionListener: GutterIconRendererActionListener
    ): GutterIconRenderer {
        val gutterIconRenderer = GutterIconRendererImpl(
            showAddIcon, visibleLineLeft, visibleLineRight, logicalLine, side, actionListener
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