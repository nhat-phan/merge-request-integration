package net.ntworld.mergeRequestIntegration.provider.gitlab.command

import net.ntworld.foundation.cqrs.Command
import net.ntworld.mergeRequest.api.ApiCredentials
import org.gitlab4j.api.models.Position

data class GitlabCreateNoteCommand(
    val credentials: ApiCredentials,
    val mergeRequestInternalId: Int,
    val position: Position?,
    val body: String
) : Command
