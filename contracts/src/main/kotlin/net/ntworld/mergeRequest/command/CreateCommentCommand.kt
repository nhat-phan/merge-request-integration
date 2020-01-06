package net.ntworld.mergeRequest.command

import net.ntworld.foundation.cqrs.Command
import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequest.CommentPosition

interface CreateCommentCommand : Command {
    val providerId: String

    val mergeRequestId: String

    val body: String

    val position: CommentPosition?

    companion object
}