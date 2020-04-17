package net.ntworld.mergeRequestIntegrationIde.diff

import com.intellij.diff.FrameDiffTool
import com.intellij.diff.util.Side
import com.intellij.openapi.Disposable
import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequest.MergeRequestInfo
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequestIntegrationIde.DataChangedSource
import net.ntworld.mergeRequestIntegrationIde.View
import net.ntworld.mergeRequestIntegrationIde.diff.gutter.GutterActionType
import net.ntworld.mergeRequestIntegrationIde.diff.gutter.GutterIconRenderer
import net.ntworld.mergeRequestIntegrationIde.diff.gutter.GutterPosition
import net.ntworld.mergeRequestIntegrationIde.diff.thread.CommentEvent
import net.ntworld.mergeRequestIntegrationIde.infrastructure.ReviewContext

interface DiffView<V : FrameDiffTool.DiffViewer> : View<DiffView.ActionListener>, Disposable {
    val viewer: V

    fun initializeLine(reviewContext: ReviewContext, visibleLine: Int, side: Side, comments: List<Comment>)

    fun prepareLine(reviewContext: ReviewContext, renderer: GutterIconRenderer, comments: List<Comment>)

    fun createGutterIcons()

    fun resetGutterIcons()

    fun destroyExistingComments(excludedVisibleLines: Set<Int>, side: Side)

    fun showAllComments()

    fun hideAllComments()

    fun resetEditorOnLine(logicalLine: Int, side: Side, repliedComment: Comment?)

    fun updateComments(visibleLine: Int, side: Side, comments: List<Comment>)

    fun displayEditorOnLine(logicalLine: Int, side: Side)

    fun displayComments(visibleLine: Int, side: Side, mode: DisplayCommentMode)

    fun displayComments(renderer: GutterIconRenderer, mode: DisplayCommentMode)

    fun scrollToLine(visibleLine: Int, side: Side)

    enum class EditorType {
        SINGLE_SIDE,
        TWO_SIDE_LEFT,
        TWO_SIDE_RIGHT,
        UNIFIED
    }

    enum class ChangeType {
        UNKNOWN,
        INSERTED,
        DELETED,
        MODIFIED
    }

    enum class DisplayCommentMode {
        TOGGLE,
        SHOW,
        HIDE
    }

    interface ActionListener : CommentEvent {
        fun onInit()

        fun onDispose()

        fun onBeforeRediff()

        fun onAfterRediff()

        fun onRediffAborted()

        fun onGutterActionPerformed(renderer: GutterIconRenderer, type: GutterActionType, mode: DisplayCommentMode)

        fun onReplyCommentRequested(content: String, repliedComment: Comment, logicalLine: Int, side: Side)

        fun onCreateCommentRequested(content: String, position: GutterPosition, logicalLine: Int, side: Side)
    }
}
