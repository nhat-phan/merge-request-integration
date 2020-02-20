package net.ntworld.mergeRequestIntegration.provider.gitlab.commandHandler

import net.ntworld.foundation.Handler
import net.ntworld.foundation.cqrs.CommandHandler
import net.ntworld.mergeRequestIntegration.provider.gitlab.GitlabClient
import net.ntworld.mergeRequestIntegration.provider.gitlab.command.GitlabCreateNoteCommand
import net.ntworld.mergeRequestIntegration.util.DateTimeUtil
import java.util.*

@Handler
class GitlabCreateNoteCommandHandler : CommandHandler<GitlabCreateNoteCommand> {

    override fun handle(command: GitlabCreateNoteCommand) = GitlabClient(
        credentials = command.credentials,
        execute = {
            val result = this.discussionsApi.createMergeRequestDiscussion(
                command.credentials.projectId.toInt(),
                command.mergeRequestInternalId,
                command.body,
                Date(),
                null, // This is a bug from the API client, null is okay
                command.position
            )
            Unit
        },
        failed = {
            throw Exception(it.message)
        }
    )

}