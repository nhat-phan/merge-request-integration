package net.ntworld.mergeRequestIntegration.provider.gitlab.response

import net.ntworld.foundation.Error
import net.ntworld.foundation.Response
import org.gitlab4j.api.models.MergeRequest

data class GitlabSearchMRsResponse(
    override val error: Error?,
    val mergeRequests: List<MergeRequest>,
    val totalPages: Int,
    val totalItems: Int,
    val currentPage: Int
): Response
