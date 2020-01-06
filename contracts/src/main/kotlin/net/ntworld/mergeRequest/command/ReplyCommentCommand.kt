package net.ntworld.mergeRequest.command

import net.ntworld.foundation.cqrs.Command
import net.ntworld.mergeRequest.Comment

interface ReplyCommentCommand : Command {
    val providerId: String

    val mergeRequestId: String

    val repliedComment: Comment

    val body: String

    companion object
}