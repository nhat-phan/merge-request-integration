package net.ntworld.mergeRequest.command

import net.ntworld.foundation.cqrs.Command

interface UnapproveMergeRequestCommand : Command {
    val providerId: String

    val mergeRequestId: String

    companion object
}