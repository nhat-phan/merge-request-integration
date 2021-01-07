package net.ntworld.mergeRequestIntegration.provider.gitlab.commandHandler

import net.ntworld.foundation.Handler
import net.ntworld.foundation.cqrs.CommandHandler
import net.ntworld.mergeRequestIntegration.provider.gitlab.GitlabFuelClient
import net.ntworld.mergeRequestIntegration.provider.gitlab.command.GitlabUpdateDiffNoteCommand

@Handler
class GitlabUpdateDiffNoteCommandHandler : CommandHandler<GitlabUpdateDiffNoteCommand> {
    override fun handle(command: GitlabUpdateDiffNoteCommand) = GitlabFuelClient(
        credentials = command.credentials,
        execute = {
            val params = listOf(
                Pair("body", command.body)
            )
            this.putJson(
                "$baseProjectUrl/merge_requests/${command.mergeRequestInternalId}/discussions/${command.discussionId}/notes/${command.noteId}",
                params
            )
            Unit
        },
        failed = {
            println(it)
        }
    )
}