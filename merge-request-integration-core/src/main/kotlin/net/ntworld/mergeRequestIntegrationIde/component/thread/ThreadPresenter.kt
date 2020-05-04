package net.ntworld.mergeRequestIntegrationIde.component.thread

import com.intellij.diff.util.Side
import com.intellij.openapi.Disposable
import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequestIntegrationIde.Presenter
import net.ntworld.mergeRequestIntegrationIde.component.comment.CommentEvent
import net.ntworld.mergeRequestIntegrationIde.component.gutter.GutterPosition

interface ThreadPresenter : Presenter<ThreadPresenter.EventListener>, Disposable {
    val model: ThreadModel

    val view: ThreadView

    interface EventListener: CommentEvent {
        fun onMainEditorClosed(threadPresenter: ThreadPresenter)

        fun onReplyCommentRequested(content: String, repliedComment: Comment, logicalLine: Int, side: Side)

        fun onCreateCommentRequested(content: String, position: GutterPosition, logicalLine: Int, side: Side)
    }
}