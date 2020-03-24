package net.ntworld.mergeRequestIntegrationIde.diff.thread

import com.intellij.util.EventDispatcher
import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequestIntegrationIde.AbstractPresenter
import net.ntworld.mergeRequestIntegrationIde.diff.DiffView
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

        override fun onCreateCommentRequested(
            content: String, logicalLine: Int, contentType: DiffView.ContentType,
            repliedComment: Comment?, position: GutterPosition?
        ) {
            if (null !== repliedComment) {
                dispatcher.multicaster.onReplyCommentRequested(content, repliedComment, logicalLine, contentType)
            }
            if (null !== position) {
                dispatcher.multicaster.onCreateCommentRequested(content, position, logicalLine, contentType)
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
        val currentGroups = view.getAllGroupOfCommentsIds().toMutableSet()

        groups.forEach { (id, items) ->
            if (view.hasGroupOfComments(id)) {
                view.updateGroupOfComments(id, items)
            } else {
                view.addGroupOfComments(id, items)
            }
            currentGroups.remove(id)
        }

        currentGroups.forEach { id ->
            view.deleteGroupOfComments(id)
        }

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

    override fun onEditorReset(comment: Comment?) {
        if (null === comment) {
            model.showEditor = false
            view.resetMainEditor()
        } else {
            view.resetEditorOfGroup(comment.parentId)
        }
    }
}