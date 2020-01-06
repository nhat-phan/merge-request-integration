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
            this.discussionsApi.createMergeRequestDiscussion(
                command.credentials.projectId.toInt(),
                command.mergeRequestInternalId,
                command.body,
                Date(),
                // if (null === command.position) null else command.position.toString(),
                null,
                command.position
            )
            Unit
        },
        failed = {
            throw Exception(it.message)
        }
    )

}