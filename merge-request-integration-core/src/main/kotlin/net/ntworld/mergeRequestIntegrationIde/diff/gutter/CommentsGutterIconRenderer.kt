package net.ntworld.mergeRequestIntegrationIde.diff.gutter

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.editor.markup.GutterIconRenderer
import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequestIntegrationIde.diff.DiffView
import net.ntworld.mergeRequestIntegrationIde.ui.util.Icons
import javax.swing.Icon

class CommentsGutterIconRenderer (
    val visibleLine: Int,
    val logicalLine: Int,
    val contentType: DiffView.ContentType,
    private val action: ((CommentsGutterIconRenderer, AnActionEvent) -> Unit)
) : GutterIconRenderer() {
    private val clickAction = object : AnAction() {
        override fun actionPerformed(e: AnActionEvent) {
            action.invoke(this@CommentsGutterIconRenderer, e)
        }
    }

    override fun isNavigateAction() = true
    override fun getClickAction() = clickAction

    override fun getIcon(): Icon = Icons.Comments
    override fun getTooltipText() = "Show comments"

    override fun hashCode(): Int = System.identityHashCode(this)
    override fun equals(other: Any?): Boolean = other == this
}