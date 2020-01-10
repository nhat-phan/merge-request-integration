package net.ntworld.mergeRequestIntegration.commandHandler

import net.ntworld.foundation.Handler
import net.ntworld.foundation.cqrs.CommandHandler
import net.ntworld.mergeRequest.command.ResolveCommentCommand
import net.ntworld.mergeRequestIntegration.ApiProviderManager

@Handler
class ResolveCommentCommandHandler : CommandHandler<ResolveCommentCommand> {
    override fun handle(command: ResolveCommentCommand) {
        val (data, api) = ApiProviderManager.findOrFail(command.providerId)
        api.comment.resolve(data.project, command.mergeRequestId, command.comment)
    }
}