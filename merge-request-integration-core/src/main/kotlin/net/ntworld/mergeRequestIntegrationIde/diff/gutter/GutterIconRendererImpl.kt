package net.ntworld.mergeRequestIntegrationIde.diff.gutter

import com.intellij.diff.util.Side
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import net.ntworld.mergeRequestIntegrationIde.component.Icons
import com.intellij.openapi.editor.markup.GutterIconRenderer as GutterIconRendererClass

class GutterIconRendererImpl(
    private val showAddIcon: Boolean,
    override val visibleLineLeft: Int?,
    override val visibleLineRight: Int?,
    override val logicalLine: Int,
    override val side: Side,
    private val action: ((GutterIconRenderer, GutterActionType) -> Unit)
) : GutterIconRenderer, GutterIconRendererClass() {
    private var icon = if (showAddIcon) Icons.Gutter.AddComment else Icons.Gutter.Empty
    private var desc = ""

    private class MyClickAction(private val self: GutterIconRendererImpl) : AnAction() {
        override fun actionPerformed(e: AnActionEvent) {
            if (self.icon == Icons.Gutter.AddComment || self.icon == Icons.Gutter.Empty) {
                self.action.invoke(self, GutterActionType.ADD)
            } else {
                self.action.invoke(self, GutterActionType.TOGGLE)
            }
        }
    }

    private val clickAction = MyClickAction(this)

    override fun setState(state: GutterState) {
        when (state) {
            GutterState.NO_COMMENT -> {
                icon = if (showAddIcon) Icons.Gutter.AddComment else Icons.Gutter.Empty
                desc = if (showAddIcon) "Add new comment" else ""
            }
            GutterState.THREAD_HAS_SINGLE_COMMENT -> {
                icon = Icons.Gutter.Comment
                desc = "Toggle comment thread"
            }
            GutterState.THREAD_HAS_MULTI_COMMENTS -> {
                icon = Icons.Gutter.Comments
                desc = "Toggle comment thread"
            }
            GutterState.WRITING -> {
                icon = Icons.Gutter.WritingComment
                desc = "Continue writing your comment"
            }
        }
    }

    override fun triggerAddAction() {
        action.invoke(this, GutterActionType.ADD)
    }

    override fun triggerToggleAction() {
        action.invoke(this, GutterActionType.TOGGLE)
    }

    override fun getClickAction(): AnAction = clickAction
    override fun getIcon() = icon
    override fun isNavigateAction() = icon != Icons.Gutter.Empty
    override fun hashCode(): Int = System.identityHashCode(this)
    override fun equals(other: Any?): Boolean = other == this
}
