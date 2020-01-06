package net.ntworld.mergeRequestIntegrationIde

import net.ntworld.foundation.InfrastructureProvider
import net.ntworld.mergeRequest.ProviderInfo
import net.ntworld.mergeRequestIntegration.MergeRequestIntegrationInfrastructure
import net.ntworld.mergeRequestIntegration.provider.gitlab.Gitlab
import net.ntworld.mergeRequestIntegration.provider.gitlab.GitlabApiProvider
import net.ntworld.mergeRequestIntegration.provider.gitlab.GitlabCredentials

class IdeInfrastructure : InfrastructureProvider() {
    private val providers = listOf(
        GitlabApiProvider(GitlabCredentials.Empty, this)
    )

    private val included = listOf(
        MergeRequestIntegrationInfrastructure(providers)
    )

    init {
        wire(this.root, included)
    }
}
