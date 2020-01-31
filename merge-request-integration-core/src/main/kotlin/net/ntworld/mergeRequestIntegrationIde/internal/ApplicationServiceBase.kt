package net.ntworld.mergeRequestIntegrationIde.internal

import net.ntworld.foundation.Infrastructure
import net.ntworld.foundation.MemorizedInfrastructure
import net.ntworld.mergeRequest.ProjectVisibility
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequest.ProviderInfo
import net.ntworld.mergeRequest.api.ApiCredentials
import net.ntworld.mergeRequestIntegration.ApiProviderManager
import net.ntworld.mergeRequestIntegrationIde.IdeInfrastructure
import net.ntworld.mergeRequestIntegrationIde.internal.option.EnableRequestCacheOption
import net.ntworld.mergeRequestIntegrationIde.internal.option.SettingOption
import net.ntworld.mergeRequestIntegrationIde.service.ApplicationService
import net.ntworld.mergeRequestIntegrationIde.service.ApplicationSettings
import net.ntworld.mergeRequestIntegrationIde.service.ProviderSettings
import org.jdom.Element

open class ApplicationServiceBase : ApplicationService, ServiceBase() {
    private val legalGrantedDomains = listOf(
        "https://gitlab.personio-internal.de"
    )
    private val myAllSettingOptions = listOf<SettingOption<*>>(
        EnableRequestCacheOption
    )
    private var myApplicationSettings : ApplicationSettings = ApplicationSettingsImpl.DEFAULT

    override fun supported(): List<ProviderInfo> = supportedProviders

    override val infrastructure: Infrastructure = MemorizedInfrastructure(IdeInfrastructure())
    override val settings: ApplicationSettings
        get() = myApplicationSettings

    override fun getState(): Element? {
        val element = super.getState()
        if (null === element) {
            return element
        }
        writeSettingOption(element, EnableRequestCacheOption, myApplicationSettings.enableRequestCache)
        return element
    }

    override fun loadState(state: Element) {
        super.loadState(state)
        var settings = ApplicationSettingsImpl.DEFAULT
        for (item in state.children) {
            if (item.name != "Setting") {
                continue
            }

            val nameAttribute = item.getAttribute("name")
            if (null === nameAttribute) {
                continue
            }

            val valueAttribute = item.getAttribute("value")
            if (null === valueAttribute) {
                continue
            }

            for (option in myAllSettingOptions) {
                if (option.name == nameAttribute.value.trim()) {
                    settings = option.readValue(valueAttribute.value, settings)
                }
            }
        }
        myApplicationSettings = settings
        ApiProviderManager.updateApiOptions(settings.toApiOptions())
    }

    override fun addProviderConfiguration(id: String, info: ProviderInfo, credentials: ApiCredentials) {
        myProvidersData[id] = ProviderSettingsImpl(
            id = id,
            info = info,
            credentials = encryptCredentials(info, credentials),
            repository = ""
        )
    }

    override fun removeAllProviderConfigurations() {
        myProvidersData.clear()
        this.state
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

    override fun updateSettings(settings: ApplicationSettings) {
        myApplicationSettings = settings
    }

    private fun<T> writeSettingOption(root: Element, option: SettingOption<T>, value: T) {
        val item = Element("Setting")
        item.setAttribute("name", option.name)
        item.setAttribute("value", option.writeValue(value))
        root.addContent(item)
    }
}