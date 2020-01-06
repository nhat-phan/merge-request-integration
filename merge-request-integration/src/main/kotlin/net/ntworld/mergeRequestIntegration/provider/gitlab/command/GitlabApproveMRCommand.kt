package net.ntworld.mergeRequestIntegration.provider.gitlab.command

import net.ntworld.foundation.cqrs.Command
import net.ntworld.mergeRequest.api.ApiCredentials

data class GitlabApproveMRCommand(
    val credentials: ApiCredentials,
    val mergeRequestInternalId: Int,
    val sha: String
) : Command
