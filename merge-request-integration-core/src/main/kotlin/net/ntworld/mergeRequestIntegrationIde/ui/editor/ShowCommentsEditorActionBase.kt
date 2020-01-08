package net.ntworld.mergeRequestIntegrationIde.ui.editor

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorAction
import com.intellij.openapi.editor.actionSystem.EditorActionHandler

open class ShowCommentsEditorActionBase : EditorAction(Handler) {
    companion object Handler: EditorActionHandler() {
        override fun doExecute(editor: Editor, caret: Caret?, dataContext: DataContext?) {
            super.doExecute(editor, caret, dataContext)
            println(editor)
            println(caret)
            println(dataContext)
        }
    }
}
