package net.ntworld.mergeRequestIntegrationIdeEE

import net.ntworld.mergeRequestIntegrationIde.ui.configuration.ConfigurationBase

class Configuration: ConfigurationBase() {
    override fun getId(): String {
        return "merge-request-integration"
    }

    override fun getDisplayName(): String {
        return "Merge Request Integration EE"
    }
}