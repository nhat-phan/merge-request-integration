package net.ntworld.mergeRequestIntegrationIde.internal

import net.ntworld.foundation.Infrastructure
import net.ntworld.foundation.MemorizedInfrastructure
import net.ntworld.mergeRequest.ProjectVisibility
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequest.ProviderInfo
import net.ntworld.mergeRequest.api.ApiCredentials
import net.ntworld.mergeRequestIntegrationIde.ENTERPRISE_EDITION_URL
import net.ntworld.mergeRequestIntegrationIde.IdeInfrastructure
import net.ntworld.mergeRequestIntegrationIde.service.ApplicationService
import net.ntworld.mergeRequestIntegrationIde.service.ProviderSettings

open class ApplicationServiceBase : ApplicationService, ServiceBase() {
    private val legalGrantedDomains = listOf(
        "https://gitlab.personio-internal.de"
    )

    override fun supported(): List<ProviderInfo> = supportedProviders

    override val infrastructure: Infrastructure = MemorizedInfrastructure(IdeInfrastructure())

    override fun addProviderConfiguration(id: String, info: ProviderInfo, credentials: ApiCredentials) {
        myProvidersData[id] = ProviderSettingsImpl(
            id = id,
            info = info,
            credentials = encryptCredentials(info, credentials),
            repository = ""
        )
    }

    override fun removeProviderConfiguration(id: String) {
        myProvidersData.remove(id)
    }

    override fun getProviderConfigurations(): List<ProviderSettings> {
        return myProvidersData.values.map {
            ProviderSettingsImpl(
                id = it.id,
                info = it.info,
                credentials = decryptCredentials(it.info, it.credentials),
                repository = ""
            )
        }
    }

    override fun isLegal(providerData: ProviderData): Boolean {
        if (providerData.project.visibility == ProjectVisibility.PUBLIC) {
            return true
        }

        for (legalGranted in legalGrantedDomains) {
            if (providerData.project.url.startsWith(legalGranted)) {
                return true
            }
        }
        return false
    }
}