package net.ntworld.mergeRequestIntegration.provider.gitlab.request

import net.ntworld.foundation.Request
import net.ntworld.mergeRequest.api.ApiCredentials
import net.ntworld.mergeRequestIntegration.provider.gitlab.GitlabRequest
import net.ntworld.mergeRequestIntegration.provider.gitlab.response.GitlabSearchMRsResponse
import org.gitlab4j.api.Constants

// FIXME: authorId, assigneeId... in this package belong to Gitlab, then it should be an integer not String
data class GitlabSearchMRsRequest(
    override val credentials: ApiCredentials,
    val state: Constants.MergeRequestState,
    val search: String,
    val authorId: String,
    val assigneeId: String,
    val approverIds: List<String>,
    val sourceBranch: String,
    val orderBy: Constants.MergeRequestOrderBy = Constants.MergeRequestOrderBy.UPDATED_AT,
    val sort: Constants.SortOrder,
    val page: Int = 0,
    val perPage: Int = 100
): GitlabRequest, Request<GitlabSearchMRsResponse>
