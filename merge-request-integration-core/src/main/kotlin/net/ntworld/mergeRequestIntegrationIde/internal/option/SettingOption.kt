package net.ntworld.mergeRequestIntegrationIde.internal.option

import net.ntworld.mergeRequestIntegrationIde.internal.ApplicationSettingsImpl

interface SettingOption<T> {
    val name: String

    fun writeValue(value: T): String

    fun readValue(input: String, currentSettings: ApplicationSettingsImpl): ApplicationSettingsImpl
}
