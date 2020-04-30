package net.ntworld.mergeRequestIntegration.commandHandler

import net.ntworld.foundation.Handler
import net.ntworld.foundation.cqrs.CommandHandler
import net.ntworld.mergeRequest.command.UnresolveCommentCommand
import net.ntworld.mergeRequestIntegration.ProviderStorage

@Handler
class UnresolveCommentCommandHandler(
    private val providerStorage: ProviderStorage
) : CommandHandler<UnresolveCommentCommand> {
    override fun handle(command: UnresolveCommentCommand) {
        val (data, api) = providerStorage.findOrFail(command.providerId)
        api.comment.unresolve(data.project, command.mergeRequestId, command.comment)
    }
}
