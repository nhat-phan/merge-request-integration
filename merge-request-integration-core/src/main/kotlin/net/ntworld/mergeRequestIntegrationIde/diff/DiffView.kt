package net.ntworld.mergeRequestIntegrationIde.diff

import com.intellij.diff.FrameDiffTool
import com.intellij.openapi.Disposable
import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequest.MergeRequest
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequestIntegrationIde.View
import net.ntworld.mergeRequestIntegrationIde.diff.gutter.GutterActionType
import net.ntworld.mergeRequestIntegrationIde.diff.gutter.GutterIconRenderer
import net.ntworld.mergeRequestIntegrationIde.diff.gutter.GutterPosition
import net.ntworld.mergeRequestIntegrationIde.diff.thread.CommentEvent

interface DiffView<V : FrameDiffTool.DiffViewer> : View<DiffView.ActionListener>, Disposable {
    val viewer: V

    fun createGutterIcons()

    fun resetGutterIcons()

    fun changeGutterIconsByComments(visibleLine: Int, contentType: ContentType, comments: List<Comment>)

    fun updateComments(visibleLine: Int, contentType: ContentType, comments: List<Comment>)

    fun displayEditorOnLine(
        providerData: ProviderData,
        mergeRequest: MergeRequest,
        logicalLine: Int,
        contentType: ContentType,
        comments: List<Comment>
    )

    fun toggleCommentsOnLine(
        providerData: ProviderData,
        mergeRequest: MergeRequest,
        logicalLine: Int,
        contentType: ContentType,
        comments: List<Comment>
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

    interface ActionListener : CommentEvent {
        fun onInit()

        fun onDispose()

        fun onBeforeRediff()

        fun onAfterRediff()

        fun onRediffAborted()

        fun onGutterActionPerformed(renderer: GutterIconRenderer, type: GutterActionType)

        fun onReplyCommentRequested(content: String, repliedComment: Comment)

        fun onCreateCommentRequested(content: String, position: GutterPosition)
    }
}
