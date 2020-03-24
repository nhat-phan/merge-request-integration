package net.ntworld.mergeRequestIntegrationIde.diff.thread

import com.intellij.openapi.Disposable
import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequestIntegrationIde.Component

interface GroupComponent : Component, Disposable {
    val id: String

    var comments: List<Comment>

    var collapse: Boolean

    fun requestDeleteComment(comment: Comment)

    fun requestToggleResolvedStateOfComment(comment: Comment)

    fun resetReplyEditor()

    fun showReplyEditor()

    fun destroyReplyEditor()

    fun addListener(listener: EventListener)

    interface EventListener : java.util.EventListener, CommentEvent {
        fun onResized()

        fun onEditorCreated(groupId: String, editor: EditorComponent)

        fun onEditorDestroyed(groupId: String, editor: EditorComponent)

        fun onReplyCommentRequested(comment: Comment, content: String)
    }
}