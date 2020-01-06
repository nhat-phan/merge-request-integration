package net.ntworld.mergeRequestIntegration.provider.gitlab.command

import net.ntworld.foundation.cqrs.Command
import net.ntworld.mergeRequest.api.ApiCredentials

data class GitlabReplyNoteCommand(
    val credentials: ApiCredentials,
    val mergeRequestInternalId: Int,
    val discussionId: String,
    val noteId: Int,
    val body: String
) : Command
