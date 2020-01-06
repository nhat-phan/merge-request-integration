package net.ntworld.mergeRequestIntegration.provider.gitlab.commandHandler

import net.ntworld.foundation.Handler
import net.ntworld.foundation.cqrs.CommandHandler
import net.ntworld.mergeRequestIntegration.provider.gitlab.command.GitlabUnapproveMRCommand
import org.gitlab4j.api.Constants
import org.gitlab4j.api.GitLabApi

@Handler
class GitlabUnapproveMRCommandHandler: CommandHandler<GitlabUnapproveMRCommand> {

    override fun handle(command: GitlabUnapproveMRCommand) {
        val api = GitLabApi(command.credentials.url, Constants.TokenType.PRIVATE, command.credentials.token)
        api.mergeRequestApi.unapproveMergeRequest(
            command.credentials.projectId.toInt(),
            command.mergeRequestInternalId
        )
    }

}
