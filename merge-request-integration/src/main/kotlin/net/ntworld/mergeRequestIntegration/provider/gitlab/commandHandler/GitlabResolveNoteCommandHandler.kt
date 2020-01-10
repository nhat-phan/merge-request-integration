package net.ntworld.mergeRequestIntegration.provider.gitlab.commandHandler

import net.ntworld.foundation.Handler
import net.ntworld.foundation.cqrs.CommandHandler
import net.ntworld.mergeRequestIntegration.provider.gitlab.GitlabFuelClient
import net.ntworld.mergeRequestIntegration.provider.gitlab.command.GitlabResolveNoteCommand

@Handler
class GitlabResolveNoteCommandHandler : CommandHandler<GitlabResolveNoteCommand> {

    override fun handle(command: GitlabResolveNoteCommand) = GitlabFuelClient(
        credentials = command.credentials,
        execute = {
            val params = listOf(
                Pair("resolved", command.resolve)
            )
            this.putJson(
                "$baseProjectUrl/merge_requests/${command.mergeRequestInternalId}/discussions/${command.discussionId}",
                params
            )
            Unit
        },
        failed = {
            println(it)
        }
    )

}