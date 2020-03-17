package net.ntworld.mergeRequestIntegrationIde.diff

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorAction
import com.intellij.openapi.editor.actionSystem.EditorActionHandler
import net.ntworld.mergeRequestIntegrationIde.diff.gutter.CommentsGutterIconRenderer

open class ToggleCommentsEditorActionBase : EditorAction(MyHandler()) {

    private class MyHandler : EditorActionHandler() {
        override fun isEnabledForCaret(editor: Editor, caret: Caret, dataContext: DataContext?): Boolean {
            return null !== findGutterIconRenderer(editor)
        }

        override fun doExecute(editor: Editor, caret: Caret?, dataContext: DataContext?) {
            super.doExecute(editor, caret, dataContext)
            val gutterRenderer = findGutterIconRenderer(editor)
            if (null !== gutterRenderer) {
                gutterRenderer.invoke()
            }
        }

        private fun findGutterIconRenderer(editor: Editor): CommentsGutterIconRenderer? {
            val logicalPosition = editor.caretModel.logicalPosition
            val line = logicalPosition.line + 1
            for (highlighter in editor.markupModel.allHighlighters) {
                val gutterRenderer = highlighter.gutterIconRenderer
                if (gutterRenderer !is CommentsGutterIconRenderer || gutterRenderer.visibleLine != line) {
                    continue
                }
                return gutterRenderer
            }
            return null
        }
    }

}