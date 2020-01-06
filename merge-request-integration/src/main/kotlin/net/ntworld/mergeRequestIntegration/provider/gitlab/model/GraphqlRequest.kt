package net.ntworld.mergeRequestIntegration.provider.gitlab.model

import kotlinx.serialization.Serializable

@Serializable
data class GraphqlRequest(
    val query: String,
    val variables: Map<String, String?>
)
