package net.ntworld.mergeRequestIntegration.provider.gitlab.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserInfoModel(
    val id: Int,

    val name: String,

    val username: String,

    @SerialName("avatar_url")
    val avatarUrl: String,

    @SerialName("web_url")
    val webUrl: String,

    val state: String
)