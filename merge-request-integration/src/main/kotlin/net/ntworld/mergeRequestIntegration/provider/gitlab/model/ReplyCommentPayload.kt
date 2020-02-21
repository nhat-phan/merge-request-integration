package net.ntworld.mergeRequestIntegration.provider.gitlab.model

import kotlinx.serialization.Serializable

@Serializable
data class ReplyCommentPayload(
    val id: Int
)
