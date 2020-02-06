package net.ntworld.mergeRequestIntegrationIde.ui.editor

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorAction
import com.intellij.openapi.editor.actionSystem.EditorActionHandler
import net.ntworld.mergeRequest.CommentPosition
import net.ntworld.mergeRequestIntegrationIde.internal.CommentStoreItem
import net.ntworld.mergeRequestIntegrationIde.service.ApplicationService
import net.ntworld.mergeRequestIntegrationIde.service.ProjectService

open class AddCommentEditorActionBase(
    private val applicationService: ApplicationService
) : EditorAction(MyHandler(applicationService)) {

    private class MyHandler(
        private val applicationService: ApplicationService
    ) : EditorActionHandler() {
        private fun findPositionForCurrentCaret(
            editor: Editor,
            caret: Caret?,
            dataContext: DataContext?
        ): CommentPosition? {
            val ideaProject = editor.project
            if (null === ideaProject) {
                return null
            }

            val projectService = applicationService.getProjectService(ideaProject)
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

            val projectService = applicationService.getProjectService(editor.project!!)
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
        }
    }

}