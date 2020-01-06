package net.ntworld.mergeRequestIntegration.update

import kotlinx.serialization.Serializable

@Serializable
data class UpdateMetadata(
    val id: Int,
    val version: String,
    val changesUrl: String,
    val active: Boolean
)
