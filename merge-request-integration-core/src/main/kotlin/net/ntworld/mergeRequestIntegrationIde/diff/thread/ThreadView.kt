package net.ntworld.mergeRequestIntegrationIde.diff.thread

import com.intellij.openapi.Disposable
import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequestIntegrationIde.View
import net.ntworld.mergeRequestIntegrationIde.diff.DiffView
import net.ntworld.mergeRequestIntegrationIde.diff.gutter.GutterPosition

interface ThreadView : View<ThreadView.ActionListener>, Disposable {
    val logicalLine: Int

    val contentType: DiffView.ContentType

    val position: GutterPosition

    fun initialize()

    fun addGroupOfComments(groupId: String, comments: List<Comment>)

    fun showEditor()

    fun show()

    fun hide()

    interface ActionListener : CommentEvent {
        fun onMainEditorClosed()

        fun onCreateCommentRequested(content: String, repliedComment: Comment?, position: GutterPosition?)
    }
}