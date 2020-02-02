package net.ntworld.mergeRequestIntegrationIdeCE

import com.intellij.openapi.components.ServiceManager
import net.ntworld.mergeRequestIntegrationIde.ui.configuration.ConfigurationBase

class Configuration: ConfigurationBase(ServiceManager.getService(ApplicationServiceImpl::class.java)) {
    override fun getId(): String {
        return "merge-request-integration-ce"
    }

    override fun getDisplayName(): String {
        return "Merge Request Integration CE"
    }
}