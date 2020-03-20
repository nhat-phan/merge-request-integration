package net.ntworld.mergeRequestIntegrationIde.diff.thread

import com.intellij.openapi.Disposable
import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequestIntegrationIde.View
import java.util.*

interface ThreadView : View<ThreadView.Action>, Disposable {
    val isEditorDisplayed: Boolean

    fun initialize()

    fun addGroupOfComments(groupId: String, comments: List<Comment>)

//    fun showReplyEditorForGroup(groupId: String)

    fun showEditor()

    fun show()

    fun hide()

    interface Action : EventListener
}