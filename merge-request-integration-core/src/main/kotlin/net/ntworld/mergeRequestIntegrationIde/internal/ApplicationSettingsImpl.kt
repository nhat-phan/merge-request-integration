package net.ntworld.mergeRequestIntegrationIde.internal

import net.ntworld.mergeRequestIntegrationIde.service.ApplicationSettings

data class ApplicationSettingsImpl(
    override val enableRequestCache: Boolean
) : ApplicationSettings {
    companion object {
        val DEFAULT = ApplicationSettingsImpl(
            enableRequestCache = true
        )
    }
}