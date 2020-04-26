package net.ntworld.mergeRequestIntegrationIde.infrastructure.option

import net.ntworld.mergeRequestIntegrationIde.infrastructure.internal.ApplicationSettingsImpl

class EnableRequestCacheOption : BooleanOption() {
    override val name: String = "enable-request-cache"

    override fun getOptionValueFromSettings(settings: ApplicationSettingsImpl): Boolean {
        return settings.enableRequestCache
    }

    override fun copySettings(settings: ApplicationSettingsImpl, value: Boolean): ApplicationSettingsImpl {
        return settings.copy(enableRequestCache = value)
    }
}