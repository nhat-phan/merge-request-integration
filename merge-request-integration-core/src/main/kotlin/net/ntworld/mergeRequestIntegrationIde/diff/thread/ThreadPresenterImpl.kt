package net.ntworld.mergeRequestIntegrationIde.diff.thread

import com.intellij.util.EventDispatcher
import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequestIntegrationIde.AbstractPresenter
import net.ntworld.mergeRequestIntegrationIde.diff.gutter.GutterIconRenderer
import net.ntworld.mergeRequestIntegrationIde.diff.gutter.GutterPosition

class ThreadPresenterImpl(
    override val model: ThreadModel,
    override val view: ThreadView
) : AbstractPresenter<ThreadPresenter.EventListener>(), ThreadPresenter, ThreadModel.DataListener {
    override val dispatcher = EventDispatcher.create(ThreadPresenter.EventListener::class.java)
    private val myCommentEventPropagator = CommentEventPropagator(dispatcher)
    private val myThreadViewActionListener = object : ThreadView.ActionListener,
        CommentEvent by myCommentEventPropagator {
        override fun onMainEditorClosed() {
            dispatcher.multicaster.onMainEditorClosed(this@ThreadPresenterImpl)
        }

        override fun onCreateCommentRequested(content: String, repliedComment: Comment?, position: GutterPosition?) {
            if (null !== repliedComment) {
                dispatcher.multicaster.onReplyCommentRequested(content, repliedComment)
            }
            if (null !== position) {
                dispatcher.multicaster.onCreateCommentRequested(content, position)
            }
        }
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
            if (model.showEditor) {
                view.showEditor()
            }
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