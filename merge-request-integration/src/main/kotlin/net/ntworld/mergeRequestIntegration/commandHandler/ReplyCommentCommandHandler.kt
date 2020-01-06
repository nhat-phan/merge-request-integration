package net.ntworld.mergeRequestIntegration.commandHandler

import net.ntworld.foundation.Handler
import net.ntworld.foundation.cqrs.CommandHandler
import net.ntworld.mergeRequest.command.ReplyCommentCommand
import net.ntworld.mergeRequestIntegration.ApiProviderManager

@Handler
class ReplyCommentCommandHandler : CommandHandler<ReplyCommentCommand> {

    override fun handle(command: ReplyCommentCommand) {
        val (data, api) = ApiProviderManager.findOrFail(command.providerId)
        api.comment.reply(
            project = data.project,
            mergeRequestId = command.mergeRequestId,
            repliedComment = command.repliedComment,
            body = command.body
        )
    }

}