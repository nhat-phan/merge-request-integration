package net.ntworld.mergeRequestIntegrationIde.internal.option

import net.ntworld.mergeRequestIntegrationIde.internal.ApplicationSettingsImpl

object EnableRequestCacheOption : SettingOption<Boolean> {
    override val name: String = "enable-request-cache"

    override fun writeValue(value: Boolean): String {
        return if (value) "1" else "0"
    }

    override fun readValue(input: String, currentSettings: ApplicationSettingsImpl): ApplicationSettingsImpl {
        val value = input.trim() == "1"
        if (currentSettings.enableRequestCache != value) {
            return currentSettings.copy(enableRequestCache = value)
        }
        return currentSettings
    }
}