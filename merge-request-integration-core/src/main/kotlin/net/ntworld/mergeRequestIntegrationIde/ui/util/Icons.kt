package net.ntworld.mergeRequestIntegrationIde.ui.util

import com.intellij.openapi.util.IconLoader

object Icons {

    val Comments = IconLoader.getIcon("/icons/comments.svg", Icons::class.java)
    val ThumbsUp = IconLoader.getIcon("/icons/thumbs-up.svg", Icons::class.java)
    val ThumbsDown = IconLoader.getIcon("/icons/thumbs-down.svg", Icons::class.java)
    val PipelineRunning = IconLoader.getIcon("/icons/clock.svg", Icons::class.java)
    val PipelineFailed = IconLoader.getIcon("/icons/exclamation-circle.svg", Icons::class.java)
    val PipelineSuccess = IconLoader.getIcon("/icons/check-circle.svg", Icons::class.java)
    val NoApproval = IconLoader.getIcon("/icons/square-gray.svg", Icons::class.java)
    val RequiredApproval = IconLoader.getIcon("/icons/square-yellow.svg", Icons::class.java)
    val Approved = IconLoader.getIcon("/icons/check-square.svg", Icons::class.java)
    val ExternalLink = IconLoader.getIcon("/icons/external-link-alt.svg", Icons::class.java)
    val Description = IconLoader.getIcon("/icons/file-alt.svg", Icons::class.java)
    val StateMerged = IconLoader.getIcon("/icons/door-closed-green.svg", Icons::class.java)
    val StateClosed = IconLoader.getIcon("/icons/door-closed-red.svg", Icons::class.java)
    val StateOpened = IconLoader.getIcon("/icons/door-open.svg", Icons::class.java)
    val HasComment = IconLoader.getIcon("/icons/sticky-note.svg", Icons::class.java)
    val ReplyComment = IconLoader.getIcon("/icons/reply.svg", Icons::class.java)
    val Trash = IconLoader.getIcon("/icons/trash.svg", Icons::class.java)
    val Resolve = IconLoader.getIcon("/icons/check-circle-gray.svg", Icons::class.java)
    val Resolved = IconLoader.getIcon("/icons/check-circle.svg", Icons::class.java)

    object Gutter {
        val Empty = IconLoader.getIcon("/icons/1px.svg", Icons::class.java)
        val Comment = IconLoader.getIcon("/icons/gutter-comment.svg", Icons::class.java)
        val Comments = IconLoader.getIcon("/icons/gutter-comments.svg", Icons::class.java)
        val AddComment = IconLoader.getIcon("/icons/gutter-plus-small.svg", Icons::class.java)
        val WritingComment = IconLoader.getIcon("/icons/gutter-writing-comment.svg", Icons::class.java)
    }

}