package net.ntworld.mergeRequestIntegration.provider.gitlab.command

import net.ntworld.foundation.cqrs.Command
import net.ntworld.mergeRequest.api.ApiCredentials

data class GitlabUnapproveMRCommand(
    val credentials: ApiCredentials,
    val mergeRequestInternalId: Int
) : Command
