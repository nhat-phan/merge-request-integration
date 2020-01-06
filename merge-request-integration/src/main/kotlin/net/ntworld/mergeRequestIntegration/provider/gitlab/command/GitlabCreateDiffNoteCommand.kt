package net.ntworld.mergeRequestIntegration.provider.gitlab.command

import net.ntworld.foundation.cqrs.Command
import net.ntworld.mergeRequest.CommentPosition
import net.ntworld.mergeRequest.api.ApiCredentials
import net.ntworld.mergeRequestIntegration.provider.gitlab.GitlabRequest

data class GitlabCreateDiffNoteCommand(
    val credentials: ApiCredentials,
    val mergeRequestInternalId: Int,
    val body: String,
    val position: CommentPosition
) : Command
