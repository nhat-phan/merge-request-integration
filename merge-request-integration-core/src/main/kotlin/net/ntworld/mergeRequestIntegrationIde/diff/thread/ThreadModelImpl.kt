package net.ntworld.mergeRequestIntegrationIde.diff.thread

import com.intellij.util.EventDispatcher
import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequestIntegrationIde.AbstractModel

class ThreadModelImpl(
    comments: List<Comment>,
     visibility: Boolean
) : AbstractModel<ThreadModel.DataListener>(), ThreadModel {
    override val dispatcher = EventDispatcher.create(ThreadModel.DataListener::class.java)

    override var comments: List<Comment> = comments
        set(value) {
            field = value
            dispatcher.multicaster.onCommentsChanged(value)
        }

    override var visible: Boolean = visibility
        set(value) {
            if (field == value) {
                return
            }
            field = value
            dispatcher.multicaster.onVisibilityChanged(value)
        }

    override var showEditor: Boolean = false
        set(value) {
            field = value
            dispatcher.multicaster.onEditorVisibilityChanged(value)
        }

    override fun resetEditor(comment: Comment?) {
        dispatcher.multicaster.onEditorReset(comment)
    }

}