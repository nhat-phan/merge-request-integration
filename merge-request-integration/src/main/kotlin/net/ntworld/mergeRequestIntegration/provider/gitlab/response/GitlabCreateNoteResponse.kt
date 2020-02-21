package net.ntworld.mergeRequestIntegration.provider.gitlab.response

import net.ntworld.foundation.Error
import net.ntworld.foundation.Response

data class GitlabCreateNoteResponse(
    override val error: Error?,
    val createdCommentId: Int
) : Response