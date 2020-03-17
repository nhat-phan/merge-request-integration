package net.ntworld.mergeRequestIntegrationIde.diff.thread

import com.intellij.util.EventDispatcher
import net.ntworld.mergeRequest.Comment

class ThreadPresenterImpl(
    override val model: ThreadModel,
    override val view: ThreadView
) : ThreadPresenter, ThreadView.Action, ThreadModel.Change {
    override val dispatcher = EventDispatcher.create(ThreadPresenter.Event::class.java)

    init {
        model.dispatcher.addListener(this)
        view.dispatcher.addListener(this)

        view.initialize()
        for (comment in model.comments) {
            view.addCommentPanel(comment)
        }
        if (model.visible) {
            view.show()
        } else {
            view.hide()
        }
    }

    override fun onCommentsChanged(comments: List<Comment>) {
    }

    override fun onVisibilityChanged(visibility: Boolean) {
        if (model.visible) {
            view.show()
        } else {
            view.hide()
        }
    }

}