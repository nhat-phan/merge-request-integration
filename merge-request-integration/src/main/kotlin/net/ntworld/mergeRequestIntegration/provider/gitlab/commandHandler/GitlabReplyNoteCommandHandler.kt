package net.ntworld.mergeRequestIntegration.provider.gitlab.commandHandler

import net.ntworld.foundation.Handler
import net.ntworld.foundation.cqrs.CommandHandler
import net.ntworld.mergeRequestIntegration.provider.gitlab.GitlabFuelClient
import net.ntworld.mergeRequestIntegration.provider.gitlab.command.GitlabReplyNoteCommand

@Handler
class GitlabReplyNoteCommandHandler : CommandHandler<GitlabReplyNoteCommand> {

    override fun handle(command: GitlabReplyNoteCommand) = GitlabFuelClient<Unit>(
        credentials = command.credentials,
        execute = {
            val url = "${baseProjectUrl}/merge_requests/${command.mergeRequestInternalId}/discussions/${command.discussionId}/notes"
            val parameters = listOf(
                Pair("body", command.body)
            )
            val result = this.postJson(url = url, parameters = parameters)
            print(result)
        },
        failed = {
        }
    )

}