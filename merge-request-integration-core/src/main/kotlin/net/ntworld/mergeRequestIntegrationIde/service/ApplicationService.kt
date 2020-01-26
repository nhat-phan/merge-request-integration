package net.ntworld.mergeRequestIntegrationIde.service

import com.intellij.openapi.components.ServiceManager
import net.ntworld.foundation.Infrastructure
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequest.ProviderInfo
import net.ntworld.mergeRequest.api.ApiCredentials

interface ApplicationService {

    val infrastructure: Infrastructure

    fun supported(): List<ProviderInfo>

    fun addProviderConfiguration(id: String, info: ProviderInfo, credentials: ApiCredentials)

    fun removeAllProviderConfigurations()

    fun getProviderConfigurations(): List<ProviderSettings>

    fun isLegal(providerData: ProviderData): Boolean

    companion object {
        val instance: ApplicationService
            get() = ServiceManager.getService(ApplicationService::class.java)
    }
}