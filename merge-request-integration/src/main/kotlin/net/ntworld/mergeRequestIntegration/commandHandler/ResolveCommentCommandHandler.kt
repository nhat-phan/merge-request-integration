package net.ntworld.mergeRequestIntegration.commandHandler

import net.ntworld.foundation.Handler
import net.ntworld.foundation.cqrs.CommandHandler
import net.ntworld.mergeRequest.command.ResolveCommentCommand
import net.ntworld.mergeRequestIntegration.ProviderStorage

@Handler
class ResolveCommentCommandHandler(
    private val providerStorage: ProviderStorage
) : CommandHandler<ResolveCommentCommand> {
    override fun handle(command: ResolveCommentCommand) {
        val (data, api) = providerStorage.findOrFail(command.providerId)
        api.comment.resolve(data.project, command.mergeRequestId, command.comment)
    }
}