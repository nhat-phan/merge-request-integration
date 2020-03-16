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
    private val editor: EditorEx,
    private val showIcon: Boolean,
    val visibleLine: Int,
    val logicalLine: Int,
    private val action: ((AddGutterIconRenderer, DiffView.ChangeType) -> Unit)
) : GutterIconRenderer() {
    private val clickAction = object : AnAction() {
        override fun actionPerformed(e: AnActionEvent) {
            action.invoke(this@AddGutterIconRenderer, findChangeType())
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

    private fun findChangeType(): DiffView.ChangeType {
        val guessChangeTypeByColorFunction = makeGuessChangeTypeByColorFunction(editor)
        val highlighters = editor.markupModel.allHighlighters
        var type = DiffView.ChangeType.UNKNOWN
        for (highlighter in highlighters) {
            val startLogicalPosition = editor.offsetToLogicalPosition(highlighter.startOffset)
            val endLogicalPosition = editor.offsetToLogicalPosition(highlighter.endOffset)
            if (startLogicalPosition.line > logicalLine || logicalLine > endLogicalPosition.line) {
                continue
            }

            val guessType = guessChangeTypeByColorFunction(highlighter.textAttributes)
            if (guessType != DiffView.ChangeType.UNKNOWN) {
                type = guessType
            }
        }
        return type
    }

    companion object {
        @JvmStatic
        fun makeGuessChangeTypeByColorFunction(editor: Editor): ((TextAttributes?) -> DiffView.ChangeType) {
            val insertedColor = TextDiffType.INSERTED.getColor(editor).rgb
            val insertedIgnoredColor = TextDiffType.INSERTED.getIgnoredColor(editor).rgb
            val deletedColor = TextDiffType.DELETED.getColor(editor).rgb
            val deletedIgnoredColor = TextDiffType.DELETED.getIgnoredColor(editor).rgb
            val modifiedColor = TextDiffType.MODIFIED.getColor(editor).rgb
            val modifiedIgnoredColor = TextDiffType.MODIFIED.getIgnoredColor(editor).rgb

            return {
                if (null === it) {
                    DiffView.ChangeType.UNKNOWN
                } else {
                    val bgColor = it.backgroundColor
                    if (null === bgColor) {
                        DiffView.ChangeType.UNKNOWN
                    } else {
                        val color = bgColor.rgb
                        when (color) {
                            insertedColor, insertedIgnoredColor -> DiffView.ChangeType.INSERTED
                            modifiedColor, modifiedIgnoredColor -> DiffView.ChangeType.MODIFIED
                            deletedColor, deletedIgnoredColor -> DiffView.ChangeType.DELETED
                            else -> DiffView.ChangeType.UNKNOWN
                        }
                    }
                }
            }
        }

    }
}