package net.ntworld.mergeRequestIntegration.commandHandler

import net.ntworld.foundation.Handler
import net.ntworld.foundation.cqrs.CommandHandler
import net.ntworld.mergeRequest.command.CreateCommentCommand
import net.ntworld.mergeRequestIntegration.ApiProviderManager

@Handler
class CreateCommentCommandHandler : CommandHandler<CreateCommentCommand> {
    override fun handle(command: CreateCommentCommand) {
        val (data, api) = ApiProviderManager.findOrFail(command.providerId)
        api.comment.create(
            project = data.project,
            mergeRequestId = command.mergeRequestId,
            body = command.body,
            position = command.position
        )
    }
}