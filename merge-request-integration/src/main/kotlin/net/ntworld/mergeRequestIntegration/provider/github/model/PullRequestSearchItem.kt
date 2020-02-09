package net.ntworld.mergeRequestIntegration.provider.github.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PullRequestSearchItem(
    val id: Long,

    val number: Int,

    @SerialName("node_id")
    val nodeId: String,

    val title: String,

    val body: String,

    @SerialName("html_url")
    val htmlUrl: String,

    val state: String,

    @SerialName("created_at")
    val createdAt: String,

    @SerialName("updated_at")
    val updatedAt: String
)