package net.ntworld.mergeRequest.command

import net.ntworld.foundation.cqrs.Command

interface ApproveMergeRequestCommand : Command {
    val providerId: String

    val mergeRequestId: String

    val sha: String

    companion object
}