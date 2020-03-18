package net.ntworld.mergeRequestIntegrationIde.diff.gutter

import com.intellij.openapi.editor.markup.RangeHighlighter
import net.ntworld.mergeRequestIntegrationIde.diff.DiffView

object GutterIconRendererFactory {
    fun makeGutterIconRenderer(
        highlighter: RangeHighlighter,
        showAddIcon: Boolean,
        logicalLine: Int,
        visibleLine: Int,
        contentType: DiffView.ContentType,
        action: ((GutterIconRenderer, GutterActionType) -> Unit)
    ): GutterIconRenderer {
        val gutterIconRenderer = GutterIconRendererImpl(
            showAddIcon, visibleLine, logicalLine, contentType, action
        )
        highlighter.gutterIconRenderer = gutterIconRenderer
        return gutterIconRenderer
    }


}