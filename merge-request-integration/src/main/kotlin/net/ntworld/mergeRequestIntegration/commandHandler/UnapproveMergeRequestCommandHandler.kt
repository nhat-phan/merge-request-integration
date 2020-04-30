package net.ntworld.mergeRequestIntegration.commandHandler

import net.ntworld.foundation.Handler
import net.ntworld.foundation.cqrs.CommandHandler
import net.ntworld.mergeRequest.command.UnapproveMergeRequestCommand
import net.ntworld.mergeRequestIntegration.ProviderStorage

@Handler
class UnapproveMergeRequestCommandHandler(
    private val providerStorage: ProviderStorage
) : CommandHandler<UnapproveMergeRequestCommand> {
    override fun handle(command: UnapproveMergeRequestCommand) {
        val (data, api) = providerStorage.findOrFail(command.providerId)
        api.mergeRequest.unapprove(
            projectId = data.project.id,
            mergeRequestId = command.mergeRequestId
        )
    }
}