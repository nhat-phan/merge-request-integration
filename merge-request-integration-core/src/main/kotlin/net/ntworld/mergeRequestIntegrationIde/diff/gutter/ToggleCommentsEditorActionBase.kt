package net.ntworld.mergeRequestIntegrationIde.diff.gutter

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorAction
import com.intellij.openapi.editor.actionSystem.EditorActionHandler

open class ToggleCommentsEditorActionBase : EditorAction(MyHandler()) {

    private class MyHandler : EditorActionHandler() {
        override fun isEnabledForCaret(editor: Editor, caret: Caret, dataContext: DataContext?): Boolean {
            return null !== GutterIconRendererFactory.findGutterIconRenderer(editor)
        }

        override fun doExecute(editor: Editor, caret: Caret?, dataContext: DataContext?) {
            super.doExecute(editor, caret, dataContext)
            val gutterRenderer = GutterIconRendererFactory.findGutterIconRenderer(editor)
            if (null !== gutterRenderer) {
                gutterRenderer.triggerToggleAction()
            }
        }
    }

}