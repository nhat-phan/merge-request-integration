package net.ntworld.mergeRequestIntegrationIde.diff.gutter

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.editor.markup.GutterIconRenderer
import net.ntworld.mergeRequestIntegrationIde.ui.util.Icons
import javax.swing.Icon

class AddGutterIconRenderer (
    private val showIcon: Boolean,
    val visibleLine: Int,
    val logicalLine: Int,
    private val action: ((AddGutterIconRenderer, AnActionEvent?) -> Unit)
) : GutterIconRenderer() {
    private val clickAction = object : AnAction() {
        override fun actionPerformed(e: AnActionEvent) {
            action.invoke(this@AddGutterIconRenderer, e)
        }
    }

    fun invoke() {
        action.invoke(this@AddGutterIconRenderer, null)
    }

    override fun isNavigateAction() = showIcon
    override fun getClickAction() = if (showIcon) clickAction else null

    override fun getIcon(): Icon = if (showIcon) Icons.AddCommentSmall else Icons.OneTransparentPixel
    override fun getTooltipText() = if (showIcon) "Add new comment" else null

    override fun hashCode(): Int = System.identityHashCode(this)
    override fun equals(other: Any?): Boolean = other == this
}