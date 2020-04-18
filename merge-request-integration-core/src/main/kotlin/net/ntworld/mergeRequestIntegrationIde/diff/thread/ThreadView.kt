package net.ntworld.mergeRequestIntegrationIde.diff.thread

import com.intellij.diff.util.Side
import com.intellij.openapi.Disposable
import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequestIntegrationIde.View
import net.ntworld.mergeRequestIntegrationIde.component.comment.CommentEvent
import net.ntworld.mergeRequestIntegrationIde.diff.gutter.GutterPosition

interface ThreadView : View<ThreadView.ActionListener>, Disposable {
    val logicalLine: Int

    val side: Side

    val position: GutterPosition

    fun initialize()

    fun getAllGroupOfCommentsIds(): Set<String>

    fun hasGroupOfComments(groupId: String): Boolean

    fun addGroupOfComments(groupId: String, comments: List<Comment>)

    fun updateGroupOfComments(groupId: String, comments: List<Comment>)

    fun deleteGroupOfComments(groupId: String)

    fun resetMainEditor()

    fun resetEditorOfGroup(groupId: String)

    fun showEditor()

    fun show()

    fun hide()

    interface ActionListener :
        CommentEvent {
        fun onMainEditorClosed()

        fun onCreateCommentRequested(
            content: String,
            logicalLine: Int,
            side: Side,
            repliedComment: Comment?,
            position: GutterPosition?
        )
    }
}