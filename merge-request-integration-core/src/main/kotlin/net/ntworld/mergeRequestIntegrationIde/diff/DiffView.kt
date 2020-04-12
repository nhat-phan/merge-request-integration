package net.ntworld.mergeRequestIntegrationIde.diff

import com.intellij.diff.FrameDiffTool
import com.intellij.openapi.Disposable
import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequest.MergeRequest
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequestIntegrationIde.DataChangedSource
import net.ntworld.mergeRequestIntegrationIde.View
import net.ntworld.mergeRequestIntegrationIde.diff.gutter.GutterActionType
import net.ntworld.mergeRequestIntegrationIde.diff.gutter.GutterIconRenderer
import net.ntworld.mergeRequestIntegrationIde.diff.gutter.GutterPosition
import net.ntworld.mergeRequestIntegrationIde.diff.thread.CommentEvent

interface DiffView<V : FrameDiffTool.DiffViewer> : View<DiffView.ActionListener>, Disposable {
    val viewer: V

    fun createGutterIcons()

    fun resetGutterIcons()

    fun destroyExistingComments(excludedVisibleLines: Set<Int>, contentType: ContentType)

    fun showAllComments()

    fun hideAllComments()

    fun changeGutterIconsByComments(visibleLine: Int, contentType: ContentType, comments: List<Comment>)

    fun resetEditor(logicalLine: Int, contentType: ContentType, repliedComment: Comment?)

    fun updateComments(
        providerData: ProviderData,
        mergeRequest: MergeRequest,
        visibleLine: Int,
        contentType: ContentType,
        comments: List<Comment>,
        requestSource: DataChangedSource
    )

    fun displayEditorOnLine(
        providerData: ProviderData,
        mergeRequest: MergeRequest,
        logicalLine: Int,
        contentType: ContentType,
        comments: List<Comment>
    )

    fun changeCommentsVisibilityOnLine(
        providerData: ProviderData,
        mergeRequest: MergeRequest,
        logicalLine: Int,
        contentType: ContentType,
        comments: List<Comment>,
        mode: DisplayCommentMode
    )

    enum class EditorType {
        SINGLE_SIDE,
        TWO_SIDE_LEFT,
        TWO_SIDE_RIGHT,
        UNIFIED
    }

    enum class ContentType {
        BEFORE,
        AFTER
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

        fun onReplyCommentRequested(
            content: String, repliedComment: Comment, logicalLine: Int, contentType: ContentType
        )

        fun onCreateCommentRequested(
            content: String, position: GutterPosition, logicalLine: Int, contentType: ContentType
        )
    }
}
