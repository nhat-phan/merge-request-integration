package net.ntworld.mergeRequestIntegrationIde.component.comment

import com.intellij.openapi.Disposable
import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequestIntegrationIde.Component

interface GroupComponent : Component, Disposable {
    val id: String

    var comments: List<Comment>

    var collapse: Boolean

    fun requestOpenDialog()

    fun requestDeleteComment(comment: Comment)

    fun requestEditComment(comment: Comment, content: String)

    fun requestToggleResolvedStateOfComment(comment: Comment)

    fun resetReplyEditor()

    fun showReplyEditor()

    fun destroyReplyEditor()

    fun editEditorCreated(comment: Comment, editor: EditorComponent)

    fun editEditorDestroyed(comment: Comment, editor: EditorComponent)

    fun publishDraftComment(comment: Comment)

    fun addListener(listener: EventListener)

    fun hideMoveToDialogButtons()

    fun showMoveToDialogButtons()

    interface EventListener : java.util.EventListener, CommentEvent {
        fun onResized()

        fun onOpenDialogClicked()

        fun onEditorCreated(groupId: String, editor: EditorComponent)

        fun onEditorDestroyed(groupId: String, editor: EditorComponent)

        fun onReplyCommentRequested(comment: Comment, content: String)

        fun onEditCommentRequested(comment: Comment, content: String)

        fun onPublishDraftCommentRequested(comment: Comment)
    }
}