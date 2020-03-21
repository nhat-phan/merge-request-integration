package net.ntworld.mergeRequestIntegrationIde.diff.thread

import com.intellij.util.EventDispatcher
import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequestIntegrationIde.AbstractPresenter

class ThreadPresenterImpl(
    override val model: ThreadModel,
    override val view: ThreadView
) : AbstractPresenter<ThreadPresenter.EventListener>(), ThreadPresenter, ThreadModel.DataListener {
    override val dispatcher = EventDispatcher.create(ThreadPresenter.EventListener::class.java)
    private val myCommentEventPropagator = CommentEventPropagator(dispatcher)
    private val myThreadViewActionListener = object : ThreadView.ActionListener,
        CommentEvent by myCommentEventPropagator {
    }

    init {
        model.addDataListener(this)
        view.addActionListener(myThreadViewActionListener)
        view.initialize()
        onCommentsChanged(model.comments)
    }

    override fun dispose() {
        view.dispose()
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

    override fun onEditorVisibilityChanged(visibility: Boolean) {
        if (visibility) {
            view.show()
            view.showEditor()
        }
    }
}