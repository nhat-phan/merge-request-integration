package net.ntworld.mergeRequestIntegrationIde

import net.ntworld.foundation.InfrastructureProvider
import net.ntworld.mergeRequestIntegration.MergeRequestIntegrationInfrastructure

class IdeInfrastructure : InfrastructureProvider() {
    private val included = listOf(
        MergeRequestIntegrationInfrastructure()
    )

    init {
        wire(this.root, included)
    }
}
