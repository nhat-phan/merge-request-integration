package net.ntworld.mergeRequestIntegration.commandHandler

import net.ntworld.foundation.Handler
import net.ntworld.foundation.cqrs.CommandHandler
import net.ntworld.mergeRequest.command.UnresolveCommentCommand
import net.ntworld.mergeRequestIntegration.ApiProviderManager

@Handler
class UnresolveCommentCommandHandler : CommandHandler<UnresolveCommentCommand> {
    override fun handle(command: UnresolveCommentCommand) {
        val (data, api) = ApiProviderManager.findOrFail(command.providerId)
        api.comment.unresolve(data.project, command.mergeRequestId, command.comment)
    }
}
