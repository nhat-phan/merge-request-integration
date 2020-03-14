package net.ntworld.mergeRequestIntegrationIde.diff.gutter

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.editor.markup.GutterIconRenderer
import net.ntworld.mergeRequestIntegrationIde.ui.util.Icons
import javax.swing.Icon

class AddGutterIconRenderer (
    val visibleLine: Int,
    val oldLine: Int?,
    val oldPath: String?,
    val newLine: Int?,
    val newPath: String?,
    private val action: ((AddGutterIconRenderer, AnActionEvent) -> Unit)
) : GutterIconRenderer() {
    private val clickAction = object : AnAction() {
        override fun actionPerformed(e: AnActionEvent) {
            action.invoke(this@AddGutterIconRenderer, e)
        }
    }

    override fun isNavigateAction() = true
    override fun getClickAction() = clickAction

    override fun getIcon(): Icon = Icons.AddCommentSmall
    override fun getTooltipText() = "Add new comment"

    override fun hashCode(): Int = System.identityHashCode(this)
    override fun equals(other: Any?): Boolean = other == this
}