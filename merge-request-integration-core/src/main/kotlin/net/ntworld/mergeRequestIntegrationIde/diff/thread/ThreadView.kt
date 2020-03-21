package net.ntworld.mergeRequestIntegrationIde.diff.thread

import com.intellij.openapi.Disposable
import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequestIntegrationIde.View

interface ThreadView : View<ThreadView.ActionListener>, Disposable {
    val isEditorDisplayed: Boolean

    fun initialize()

    fun addGroupOfComments(groupId: String, comments: List<Comment>)

    fun showEditor()

    fun show()

    fun hide()

    interface ActionListener : CommentEvent
}