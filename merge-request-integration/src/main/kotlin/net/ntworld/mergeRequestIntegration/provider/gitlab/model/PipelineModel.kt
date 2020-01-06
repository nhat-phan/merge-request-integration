package net.ntworld.mergeRequestIntegration.provider.gitlab.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PipelineModel(
    val id: Int,

    val sha: String,

    val ref: String,

    val status: String,

    @SerialName("created_at")
    val createdAt: String,

    @SerialName("updated_at")
    val updatedAt: String,

    @SerialName("web_url")
    val webUrl: String
)