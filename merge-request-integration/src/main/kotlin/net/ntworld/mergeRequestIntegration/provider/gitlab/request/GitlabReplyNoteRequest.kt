package net.ntworld.mergeRequestIntegration.provider.gitlab.request

import net.ntworld.foundation.Request
import net.ntworld.mergeRequest.api.ApiCredentials
import net.ntworld.mergeRequestIntegration.provider.gitlab.GitlabRequest
import net.ntworld.mergeRequestIntegration.provider.gitlab.response.GitlabReplyNoteResponse

data class GitlabReplyNoteRequest(
    override val credentials: ApiCredentials,
    val mergeRequestInternalId: Int,
    val discussionId: String,
    val noteId: Int,
    val body: String
) : GitlabRequest, Request<GitlabReplyNoteResponse>
