package net.ntworld.mergeRequestIntegration.provider.gitlab.model

import kotlinx.serialization.Serializable

@Serializable
data class ApproverModel(
    val user: UserInfoModel
)