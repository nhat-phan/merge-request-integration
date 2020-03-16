package net.ntworld.mergeRequestIntegrationIde.diff.thread

import com.intellij.util.EventDispatcher

class ThreadPresenterImpl(
    override val model: ThreadModel,
    override val view: ThreadView
) : ThreadPresenter, ThreadView.Action {
    override val dispatcher = EventDispatcher.create(ThreadPresenter.Event::class.java)

    init {
        for (comment in model.comments) {
            view.addCommentPanel(comment)
        }
        view.initialize()
        if (model.visible) {
            view.show()
        } else {
            view.hide()
        }
    }

}