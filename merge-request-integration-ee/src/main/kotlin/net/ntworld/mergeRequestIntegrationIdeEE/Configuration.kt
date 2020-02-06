package net.ntworld.mergeRequestIntegrationIdeEE

import com.intellij.openapi.components.ServiceManager
import net.ntworld.mergeRequestIntegrationIde.ui.configuration.ConfigurationBase

class Configuration: ConfigurationBase(ServiceManager.getService(EnterpriseApplicationService::class.java)) {
    override fun getId(): String {
        return "merge-request-integration-ee"
    }

    override fun getDisplayName(): String {
        return "Merge Request Integration EE"
    }
}