package net.ntworld.mergeRequestIntegration.provider.gitlab.commandHandler

import net.ntworld.foundation.Handler
import net.ntworld.foundation.cqrs.CommandHandler
import net.ntworld.mergeRequestIntegration.provider.gitlab.command.GitlabApproveMRCommand
import org.gitlab4j.api.Constants
import org.gitlab4j.api.GitLabApi

@Handler
class GitlabApproveMRCommandHandler : CommandHandler<GitlabApproveMRCommand> {

    override fun handle(command: GitlabApproveMRCommand) {
        val api = GitLabApi(command.credentials.url, Constants.TokenType.PRIVATE, command.credentials.token)
        api.mergeRequestApi.approveMergeRequest(
            command.credentials.projectId.toInt(),
            command.mergeRequestInternalId,
            command.sha
        )
    }

}
