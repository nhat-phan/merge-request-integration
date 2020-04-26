package net.ntworld.mergeRequestIntegrationIde.infrastructure.internal

import net.ntworld.mergeRequest.ProviderInfo
import net.ntworld.mergeRequest.api.ApiCredentials
import net.ntworld.mergeRequestIntegrationIde.infrastructure.ProviderSettings

data class ProviderSettingsImpl(
    override val id: String,
    override val info: ProviderInfo,
    override val credentials: ApiCredentials,
    override val repository: String,
    override val sharable: Boolean = false
) : ProviderSettings