package net.ntworld.mergeRequestIntegrationIde.diff.gutter

import com.intellij.diff.util.TextDiffType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.editor.markup.TextAttributes
import net.ntworld.mergeRequestIntegrationIde.diff.DiffView
import net.ntworld.mergeRequestIntegrationIde.ui.util.Icons
import javax.swing.Icon

class AddGutterIconRenderer (
    private val showIcon: Boolean,
    val visibleLine: Int,
    val logicalLine: Int,
    private val action: ((AddGutterIconRenderer, DiffView.ChangeType?) -> Unit)
) : GutterIconRenderer() {
    private val clickAction = object : AnAction() {
        override fun actionPerformed(e: AnActionEvent) {
            action.invoke(this@AddGutterIconRenderer, null)
        }
    }

    fun invoke(changeType: DiffView.ChangeType) {
        action.invoke(this@AddGutterIconRenderer, changeType)
    }

    override fun isNavigateAction() = showIcon
    override fun getClickAction() = if (showIcon) clickAction else null

    override fun getIcon(): Icon = if (showIcon) Icons.AddCommentSmall else Icons.OneTransparentPixel
    override fun getTooltipText() = if (showIcon) "Add new comment" else null

    override fun hashCode(): Int = System.identityHashCode(this)
    override fun equals(other: Any?): Boolean = other == this


}