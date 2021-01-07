package net.ntworld.mergeRequestIntegrationIde.component.thread

import com.intellij.diff.util.Side
import com.intellij.util.EventDispatcher
import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequestIntegrationIde.AbstractPresenter
import net.ntworld.mergeRequestIntegrationIde.component.comment.CommentEvent
import net.ntworld.mergeRequestIntegrationIde.component.comment.CommentEventPropagator
import net.ntworld.mergeRequestIntegrationIde.component.gutter.GutterPosition
import net.ntworld.mergeRequestIntegrationIde.util.CommentUtil

class ThreadPresenterImpl(
    override val model: ThreadModel,
    override val view: ThreadView
) : AbstractPresenter<ThreadPresenter.EventListener>(), ThreadPresenter, ThreadModel.DataListener {
    override val dispatcher = EventDispatcher.create(ThreadPresenter.EventListener::class.java)
    private val myCommentEventPropagator =
        CommentEventPropagator(dispatcher)
    private val myThreadViewActionListener = object : ThreadView.ActionListener,
        CommentEvent by myCommentEventPropagator {
        override fun onMainEditorClosed() {
            dispatcher.multicaster.onMainEditorClosed(this@ThreadPresenterImpl)
        }

        override fun onEditCommentRequested(comment: Comment, content: String) {
            dispatcher.multicaster.onEditCommentRequested(comment, content)
        }

        override fun onCreateCommentRequested(
            content: String, logicalLine: Int, side: Side,
            repliedComment: Comment?, position: GutterPosition?,
            isDraft: Boolean
        ) {
            if (null !== repliedComment) {
                dispatcher.multicaster.onReplyCommentRequested(content, repliedComment, logicalLine, side)
            }
            if (null !== position) {
                dispatcher.multicaster.onCreateCommentRequested(content, position, logicalLine, side, isDraft)
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
        val groups = CommentUtil.groupCommentsByThreadId(model.comments)
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