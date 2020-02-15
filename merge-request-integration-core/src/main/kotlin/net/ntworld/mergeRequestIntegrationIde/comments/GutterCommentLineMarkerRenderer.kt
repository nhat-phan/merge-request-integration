package net.ntworld.mergeRequestIntegrationIde.comments

import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.DumbAwareAction
import net.ntworld.mergeRequestIntegrationIde.ui.util.Icons
import javax.swing.Icon

class GutterCommentLineMarkerRenderer(
        val line: Int,
        private val action: DumbAwareAction
) : GutterIconRenderer(), DumbAware {
    override fun isNavigateAction() = true
    override fun getClickAction() = action

    override fun getIcon(): Icon = Icons.Comments
    override fun getTooltipText() = "Show comments"

    override fun hashCode(): Int = System.identityHashCode(this)
    override fun equals(other: Any?): Boolean = other == this
}