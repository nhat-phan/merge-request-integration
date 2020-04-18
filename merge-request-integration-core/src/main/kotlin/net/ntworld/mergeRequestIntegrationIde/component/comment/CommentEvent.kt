package net.ntworld.mergeRequestIntegrationIde.component.comment

import net.ntworld.mergeRequest.Comment
import java.util.*

interface CommentEvent: EventListener {

    fun onDeleteCommentRequested(comment: Comment)

    fun onResolveCommentRequested(comment: Comment)

    fun onUnresolveCommentRequested(comment: Comment)

}