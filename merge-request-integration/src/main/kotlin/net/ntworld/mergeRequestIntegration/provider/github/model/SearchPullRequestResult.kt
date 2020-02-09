package net.ntworld.mergeRequestIntegration.provider.github.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SearchPullRequestResult(
    @SerialName("total_count")
    val totalCount: Int,

    @SerialName("incomplete_results")
    val incompleteResults: Boolean,

    val items: List<PullRequestSearchItem>
)