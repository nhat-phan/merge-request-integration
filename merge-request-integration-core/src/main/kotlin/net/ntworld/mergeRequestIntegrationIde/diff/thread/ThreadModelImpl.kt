package net.ntworld.mergeRequestIntegrationIde.diff.thread

import com.intellij.util.EventDispatcher
import net.ntworld.mergeRequest.Comment

class ThreadModelImpl(
    comments: List<Comment>,
    visibility: Boolean
) : ThreadModel {

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

    override var writingBody: String? = null

    override val dispatcher = EventDispatcher.create(ThreadModel.Change::class.java)

}