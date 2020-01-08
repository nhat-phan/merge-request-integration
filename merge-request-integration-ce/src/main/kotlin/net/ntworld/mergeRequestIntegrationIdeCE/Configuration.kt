package net.ntworld.mergeRequestIntegrationIdeCE

import net.ntworld.mergeRequestIntegrationIde.ui.configuration.ConfigurationBase

class Configuration: ConfigurationBase() {
    override fun getId(): String {
        return "merge-request-integration"
    }

    override fun getDisplayName(): String {
        return "Merge Request Integration CE"
    }
}