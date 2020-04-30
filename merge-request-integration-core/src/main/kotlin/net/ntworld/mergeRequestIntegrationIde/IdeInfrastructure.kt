package net.ntworld.mergeRequestIntegrationIde

import net.ntworld.foundation.InfrastructureProvider
import net.ntworld.mergeRequestIntegration.MergeRequestIntegrationInfrastructure
import net.ntworld.mergeRequestIntegration.ProviderStorage

class IdeInfrastructure(
    private val providerStorage: ProviderStorage
) : InfrastructureProvider() {
    private val included = listOf(
        MergeRequestIntegrationInfrastructure(providerStorage)
    )

    init {
        wire(this.root, included)
    }
}
