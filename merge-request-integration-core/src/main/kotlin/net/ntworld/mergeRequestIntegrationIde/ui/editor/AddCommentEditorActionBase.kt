package net.ntworld.mergeRequestIntegrationIde.ui.editor

import com.intellij.diff.util.TextDiffType
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorAction
import com.intellij.openapi.editor.actionSystem.EditorActionHandler
import net.ntworld.mergeRequestIntegrationIde.diff.DiffView
import net.ntworld.mergeRequestIntegrationIde.diff.gutter.AddGutterIconRenderer
import net.ntworld.mergeRequestIntegrationIde.service.ApplicationService

open class AddCommentEditorActionBase(
    private val applicationService: ApplicationService
) : EditorAction(MyHandler(applicationService)) {

    private class MyHandler(
        private val applicationService: ApplicationService
    ) : EditorActionHandler() {
        override fun isEnabledForCaret(editor: Editor, caret: Caret, dataContext: DataContext?): Boolean {
            val logicalPosition = editor.caretModel.logicalPosition
            val line = logicalPosition.line + 1
            for (highlighter in editor.markupModel.allHighlighters) {
                val gutterRenderer = highlighter.gutterIconRenderer
                if (gutterRenderer !is AddGutterIconRenderer || gutterRenderer.visibleLine != line) {
                    continue
                }
                return true
            }
            return false
        }

        override fun doExecute(editor: Editor, caret: Caret?, dataContext: DataContext?) {
            super.doExecute(editor, caret, dataContext)
            val logicalPosition = editor.caretModel.logicalPosition
            val offset = editor.caretModel.offset
            val line = logicalPosition.line + 1
            val guessChangeTypeByColorFunction = AddGutterIconRenderer.makeGuessChangeTypeByColorFunction(editor)

            val highlighters = editor.markupModel.allHighlighters
            var gutterRenderer : AddGutterIconRenderer? = null
            var type = DiffView.ChangeType.UNKNOWN
            for (highlighter in highlighters) {
                if (highlighter.startOffset <= offset && offset <= highlighter.endOffset) {
                    val guessType = guessChangeTypeByColorFunction(highlighter.textAttributes)
                    if (guessType != DiffView.ChangeType.UNKNOWN) {
                        type = guessType
                    }
                }
                val renderer = highlighter.gutterIconRenderer
                if (renderer !is AddGutterIconRenderer || renderer.visibleLine != line) {
                    continue
                }
                gutterRenderer = renderer
            }

            if (null !== gutterRenderer) {
                gutterRenderer.invoke(type)
            }
        }
    }

}