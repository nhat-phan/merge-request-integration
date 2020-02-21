package net.ntworld.mergeRequestIntegration.provider.gitlab.request

import net.ntworld.foundation.Request
import net.ntworld.mergeRequest.api.ApiCredentials
import net.ntworld.mergeRequestIntegration.provider.gitlab.GitlabRequest
import net.ntworld.mergeRequestIntegration.provider.gitlab.response.GitlabCreateNoteResponse
import org.gitlab4j.api.models.Position

data class GitlabCreateNoteRequest(
    override val credentials: ApiCredentials,
    val mergeRequestInternalId: Int,
    val position: Position?,
    val body: String
) : GitlabRequest, Request<GitlabCreateNoteResponse>
