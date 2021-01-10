package net.ntworld.mergeRequestIntegrationIde.component

import com.intellij.openapi.util.IconLoader

object Icons {

    val Comments = IconLoader.getIcon("/icons/comments.svg", this::class.java)
    val ThumbsUp = IconLoader.getIcon("/icons/thumbs-up.svg", this::class.java)
    val ThumbsDown = IconLoader.getIcon("/icons/thumbs-down.svg", this::class.java)
    val PipelineRunning = IconLoader.getIcon("/icons/clock.svg", this::class.java)
    val PipelineFailed = IconLoader.getIcon("/icons/exclamation-circle.svg", this::class.java)
    val PipelineSuccess = IconLoader.getIcon("/icons/check-circle.svg", this::class.java)
    val NoApproval = IconLoader.getIcon("/icons/square-gray.svg", this::class.java)
    val RequiredApproval = IconLoader.getIcon("/icons/square-yellow.svg", this::class.java)
    val Approved = IconLoader.getIcon("/icons/check-square.svg", this::class.java)
    val ExternalLink = IconLoader.getIcon("/icons/external-link-alt.svg", this::class.java)
    val Description = IconLoader.getIcon("/icons/file-alt.svg", this::class.java)
    val StateMerged = IconLoader.getIcon("/icons/door-closed-green.svg", this::class.java)
    val StateClosed = IconLoader.getIcon("/icons/door-closed-red.svg", this::class.java)
    val StateOpened = IconLoader.getIcon("/icons/door-open.svg", this::class.java)
    val HasComment = IconLoader.getIcon("/icons/sticky-note.svg", this::class.java)
    val ReplyComment = IconLoader.getIcon("/icons/reply.svg", this::class.java)
    val Trash = IconLoader.getIcon("/icons/trash.svg", this::class.java)
    val Edit = IconLoader.getIcon("/icons/edit.svg", this::class.java)
    val Resolve = IconLoader.getIcon("/icons/check-circle-gray.svg", this::class.java)
    val Resolved = IconLoader.getIcon("/icons/check-circle.svg", this::class.java)
    val CaretDown = IconLoader.getIcon("/icons/chevron-down.svg", this::class.java)
    val CaretRight = IconLoader.getIcon("/icons/chevron-right.svg", this::class.java)
    val LegalWarning = IconLoader.getIcon("/icons/exclamation-triangle.svg", this::class.java)

    object Gutter {
        val Empty = IconLoader.getIcon("/icons/1px.svg", this::class.java)
        val Comment = IconLoader.getIcon("/icons/gutter-comment.svg", this::class.java)
        val Comments = IconLoader.getIcon("/icons/gutter-comments.svg", this::class.java)
        val AddComment = IconLoader.getIcon("/icons/gutter-plus-small.svg", this::class.java)
        val WritingComment = IconLoader.getIcon("/icons/gutter-writing-comment.svg", this::class.java)
        val HasDraft = IconLoader.getIcon("/icons/edit.svg", this::class.java)
    }

    object TreeNode {
        val ResolvedComment = IconLoader.getIcon("/icons/check-square.svg", this::class.java)
        val UnresolvedComment = IconLoader.getIcon("/icons/square-yellow.svg", this::class.java)
    }
}