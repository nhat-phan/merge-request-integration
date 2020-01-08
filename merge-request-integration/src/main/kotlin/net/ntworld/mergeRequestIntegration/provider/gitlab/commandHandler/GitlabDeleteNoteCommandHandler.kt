package net.ntworld.mergeRequestIntegration.provider.gitlab.commandHandler

import net.ntworld.foundation.Handler
import net.ntworld.foundation.cqrs.CommandHandler
import net.ntworld.mergeRequestIntegration.provider.gitlab.GitlabFuelClient
import net.ntworld.mergeRequestIntegration.provider.gitlab.command.GitlabDeleteNoteCommand

@Handler
class GitlabDeleteNoteCommandHandler : CommandHandler<GitlabDeleteNoteCommand> {

    override fun handle(command: GitlabDeleteNoteCommand) = GitlabFuelClient(
        credentials = command.credentials,
        execute = {
            this.deleteJson("$baseProjectUrl/merge_requests/${command.mergeRequestInternalId}/discussions/${command.discussionId}/notes/${command.noteId}")
            Unit
        },
        failed = {}
    )

}