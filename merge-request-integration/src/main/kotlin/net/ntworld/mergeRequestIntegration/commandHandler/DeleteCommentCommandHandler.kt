package net.ntworld.mergeRequestIntegration.commandHandler

import net.ntworld.foundation.Handler
import net.ntworld.foundation.cqrs.CommandHandler
import net.ntworld.mergeRequest.command.DeleteCommentCommand
import net.ntworld.mergeRequestIntegration.ApiProviderManager

@Handler
class DeleteCommentCommandHandler : CommandHandler<DeleteCommentCommand> {
    override fun handle(command: DeleteCommentCommand) {
        val (data, api) = ApiProviderManager.findOrFail(command.providerId)
        api.comment.delete(data.project, command.mergeRequestId, command.comment)
    }
}
