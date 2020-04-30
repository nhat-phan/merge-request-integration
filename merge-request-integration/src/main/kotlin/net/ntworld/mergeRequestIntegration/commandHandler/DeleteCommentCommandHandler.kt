package net.ntworld.mergeRequestIntegration.commandHandler

import net.ntworld.foundation.Handler
import net.ntworld.foundation.cqrs.CommandHandler
import net.ntworld.mergeRequest.command.DeleteCommentCommand
import net.ntworld.mergeRequestIntegration.ProviderStorage

@Handler
class DeleteCommentCommandHandler(
    private val providerStorage: ProviderStorage
) : CommandHandler<DeleteCommentCommand> {
    override fun handle(command: DeleteCommentCommand) {
        val (data, api) = providerStorage.findOrFail(command.providerId)
        api.comment.delete(data.project, command.mergeRequestId, command.comment)
    }
}
