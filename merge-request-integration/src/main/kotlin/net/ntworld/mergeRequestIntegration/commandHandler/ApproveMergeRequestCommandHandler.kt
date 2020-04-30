package net.ntworld.mergeRequestIntegration.commandHandler

import net.ntworld.foundation.Handler
import net.ntworld.foundation.cqrs.CommandHandler
import net.ntworld.mergeRequest.command.ApproveMergeRequestCommand
import net.ntworld.mergeRequestIntegration.ProviderStorage

@Handler
class ApproveMergeRequestCommandHandler(
    private val providerStorage: ProviderStorage
) : CommandHandler<ApproveMergeRequestCommand> {
    override fun handle(command: ApproveMergeRequestCommand) {
        val (data, api) = providerStorage.findOrFail(command.providerId)
        api.mergeRequest.approve(
            projectId = data.project.id,
            mergeRequestId = command.mergeRequestId,
            sha = command.sha
        )
    }
}