package net.ntworld.mergeRequestIntegrationIde.infrastructure.setting.option

import net.ntworld.mergeRequestIntegrationIde.infrastructure.setting.ApplicationSettingsImpl

interface SettingOption<T> {
    val name: String

    fun writeValue(value: T): String

    fun readValue(input: String, currentSettings: ApplicationSettingsImpl): ApplicationSettingsImpl
}
