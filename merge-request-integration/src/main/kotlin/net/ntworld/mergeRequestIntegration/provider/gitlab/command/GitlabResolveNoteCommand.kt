package net.ntworld.mergeRequestIntegration.provider.gitlab.command

import net.ntworld.foundation.cqrs.Command
import net.ntworld.mergeRequest.api.ApiCredentials

data class GitlabResolveNoteCommand(
    val credentials: ApiCredentials,
    val mergeRequestInternalId: Int,
    val discussionId: String,
    val resolve: Boolean
) : Command
