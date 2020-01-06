package net.ntworld.mergeRequestIntegrationIde.ui.editor

import com.intellij.diff.comparison.ComparisonManager
import com.intellij.diff.comparison.ComparisonPolicy
import com.intellij.diff.tools.fragmented.UnifiedFragmentBuilder
import com.intellij.diff.tools.util.SimpleDiffPanel
import com.intellij.diff.util.DiffUtil
import com.intellij.diff.util.Side
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.LogicalPosition
import com.intellij.openapi.editor.actionSystem.EditorAction
import com.intellij.openapi.editor.actionSystem.EditorActionHandler
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.editor.impl.DocumentImpl
import com.intellij.openapi.editor.impl.DocumentMarkupModel
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.progress.DumbProgressIndicator
import com.intellij.openapi.vcs.changes.Change
import gnu.trove.TIntFunction
import net.ntworld.mergeRequest.CommentPosition
import net.ntworld.mergeRequestIntegrationIde.internal.CommentStoreItem
import net.ntworld.mergeRequestIntegrationIde.service.ProjectService
import net.ntworld.mergeRequestIntegrationIde.ui.util.Icons
import java.awt.Component
import java.util.*
import javax.swing.Icon

open class AddCommentEditorActionBase : EditorAction(Handler) {

    companion object Handler : EditorActionHandler() {
        private val myGutterIconRenderer = object : GutterIconRenderer() {
            override fun hashCode(): Int {
                return System.identityHashCode(this)
            }

            override fun getIcon(): Icon {
                return Icons.HasComment
            }

            override fun equals(other: Any?): Boolean {
                return other === this
            }
        }

        private fun findPositionForCurrentCaret(
            editor: Editor,
            caret: Caret?,
            dataContext: DataContext?
        ): CommentPosition? {
            val ideaProject = editor.project
            if (null === ideaProject) {
                return null
            }

            val projectService = ProjectService.getInstance(ideaProject)
            val codeReviewManager = projectService.codeReviewManager
            if (null === codeReviewManager) {
                return null
            }

            return codeReviewManager.findCommentPosition(editor, caret, dataContext)
        }

        override fun isEnabledForCaret(editor: Editor, caret: Caret, dataContext: DataContext?): Boolean {
            val position = findPositionForCurrentCaret(editor, caret, dataContext)
            if (null === position) {
                return false
            }

            if (position.oldLine == -1 && position.newLine == -1) {
                return false
            }
            return true
        }

        override fun doExecute(editor: Editor, caret: Caret?, dataContext: DataContext?) {
            super.doExecute(editor, caret, dataContext)
            val position = findPositionForCurrentCaret(editor, caret, dataContext)
            if (null === position) {
                return
            }

            println("--------")
            println("startHash ${position.startHash}")
            println("baseHash ${position.baseHash}")
            println("headHash ${position.headHash}")
            println("oldPath ${position.oldPath}")
            println("newPath ${position.newPath}")
            println("oldLine ${position.oldLine}")
            println("newLine ${position.newLine}")

            val projectService = ProjectService.getInstance(editor.project!!)
            val providerData = projectService.codeReviewManager!!.providerData
            val mergeRequest = projectService.codeReviewManager!!.mergeRequest
            val item = CommentStoreItem.createNewItem(
                providerData, mergeRequest,
                projectService.codeReviewUtil.convertPositionToCommentNodeData(position),
                position
            )
            projectService.commentStore.add(item)
            projectService.dispatcher.multicaster.newCommentRequested(
                providerData, mergeRequest, position, item
            )
//            val document = editor.document
//            val makeupModel = DocumentMarkupModel.forDocument(document, ideaProject, true)
//            val highlighter = makeupModel.addLineHighlighter(line, 0, null)
//            highlighter.gutterIconRenderer = myGutterIconRenderer
        }
    }

}