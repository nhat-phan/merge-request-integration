package net.ntworld.mergeRequestIntegrationIde.ui.editor

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorAction
import com.intellij.openapi.editor.actionSystem.EditorActionHandler
import net.ntworld.mergeRequestIntegrationIde.diff.gutter.AddGutterIconRenderer
import net.ntworld.mergeRequestIntegrationIde.service.ApplicationService

open class AddCommentEditorActionBase(
    private val applicationService: ApplicationService
) : EditorAction(MyHandler(applicationService)) {

    private class MyHandler(
        private val applicationService: ApplicationService
    ) : EditorActionHandler() {
        override fun isEnabledForCaret(editor: Editor, caret: Caret, dataContext: DataContext?): Boolean {
            return null !== findGutterIconRenderer(editor)
        }

        override fun doExecute(editor: Editor, caret: Caret?, dataContext: DataContext?) {
            super.doExecute(editor, caret, dataContext)
            val gutterIconRenderer = findGutterIconRenderer(editor)
            if (null !== gutterIconRenderer) {
                gutterIconRenderer.invoke()
            }
        }

        private fun findGutterIconRenderer(editor: Editor): AddGutterIconRenderer? {
            val logicalPosition = editor.caretModel.logicalPosition
            val line = logicalPosition.line + 1
            for (highlighter in editor.markupModel.allHighlighters) {
                val gutterRenderer = highlighter.gutterIconRenderer
                if (gutterRenderer !is AddGutterIconRenderer || gutterRenderer.visibleLine != line) {
                    continue
                }
                return gutterRenderer
            }
            return null
        }
    }

}