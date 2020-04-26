package net.ntworld.mergeRequestIntegrationIde.infrastructure.option

import net.ntworld.mergeRequestIntegrationIde.infrastructure.internal.ApplicationSettingsImpl

abstract class BooleanOption : SettingOption<Boolean> {
    protected abstract fun getOptionValueFromSettings(settings: ApplicationSettingsImpl) : Boolean

    protected abstract fun copySettings(settings: ApplicationSettingsImpl, value: Boolean) : ApplicationSettingsImpl

    final override fun writeValue(value: Boolean): String {
        return if (value) "1" else "0"
    }

    final override fun readValue(input: String, currentSettings: ApplicationSettingsImpl): ApplicationSettingsImpl {
        val value = input.trim() == "1"
        if (getOptionValueFromSettings(currentSettings) != value) {
            return copySettings(currentSettings, value)
        }
        return currentSettings
    }
}