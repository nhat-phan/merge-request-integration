package net.ntworld.mergeRequestIntegrationIde.service

import net.ntworld.foundation.Infrastructure
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequest.ProviderInfo
import net.ntworld.mergeRequest.api.ApiCredentials

interface ApplicationService {

    val infrastructure: Infrastructure

    val settings: ApplicationSettings

    fun supported(): List<ProviderInfo>

    fun addProviderConfiguration(id: String, info: ProviderInfo, credentials: ApiCredentials)

    fun removeAllProviderConfigurations()

    fun getProviderConfigurations(): List<ProviderSettings>

    fun isLegal(providerData: ProviderData): Boolean

    fun updateSettings(settings: ApplicationSettings)
}