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
        onCommentsChanged(model.comments)
    }

    override fun onCommentsChanged(comments: List<Comment>) {
        val groups = mutableMapOf<String, MutableList<Comment>>()
        for (comment in model.comments) {
            if (!groups.containsKey(comment.parentId)) {
                groups[comment.parentId] = mutableListOf()
            }
            groups[comment.parentId]!!.add(comment)
        }
        groups.forEach { (id, items) -> view.addGroupOfComments(id, items) }
        view.showEditor()
        if (model.visible) {
            view.show()
        } else {
            view.hide()
        }
    }

    override fun onVisibilityChanged(visibility: Boolean) {
        if (model.visible) {
            view.show()
        } else {
            view.hide()
        }
    }

}