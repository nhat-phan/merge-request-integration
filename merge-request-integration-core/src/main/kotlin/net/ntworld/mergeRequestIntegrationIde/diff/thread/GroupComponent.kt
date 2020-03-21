package net.ntworld.mergeRequestIntegrationIde.diff.thread

import com.intellij.openapi.Disposable
import com.intellij.util.EventDispatcher
import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequestIntegrationIde.Component
import java.util.*

interface GroupComponent : Component, Disposable {
    val id: String

    val comments: List<Comment>

    val dispatcher: EventDispatcher<Event>

    var collapse: Boolean

    fun showReplyEditor()

    fun destroyReplyEditor()

    interface Event : EventListener {
        fun onResized()

        fun onEditorCreated(groupId: String, editor: EditorComponent)

        fun onEditorDestroyed(groupId: String, editor: EditorComponent)
    }
}