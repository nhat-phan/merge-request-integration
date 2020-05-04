package net.ntworld.mergeRequestIntegrationIde.component.thread

import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequestIntegrationIde.Model
import java.util.*

interface ThreadModel : Model<ThreadModel.DataListener> {
    var comments: List<Comment>

    var visible: Boolean

    var showEditor: Boolean

    fun resetEditor(comment: Comment?)

    interface DataListener : EventListener {
        fun onCommentsChanged(comments: List<Comment>)

        fun onVisibilityChanged(visibility: Boolean)

        fun onEditorVisibilityChanged(visibility: Boolean)

        fun onEditorReset(comment: Comment?)
    }
}
