package net.ntworld.mergeRequestIntegrationIde.diff.thread

import com.intellij.util.EventDispatcher
import net.ntworld.mergeRequest.Comment

class CommentEventPropagator<T: CommentEvent>(
    private val dispatcher: EventDispatcher<T>
) : CommentEvent {

    override fun onDeleteCommentRequested(comment: Comment) {
        dispatcher.multicaster.onDeleteCommentRequested(comment)
    }

    override fun onResolveCommentRequested(comment: Comment) {
        dispatcher.multicaster.onResolveCommentRequested(comment)
    }

    override fun onUnresolveCommentRequested(comment: Comment) {
        dispatcher.multicaster.onUnresolveCommentRequested(comment)
    }

}