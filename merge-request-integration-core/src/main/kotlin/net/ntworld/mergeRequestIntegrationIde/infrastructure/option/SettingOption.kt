package net.ntworld.mergeRequestIntegrationIde.infrastructure.option

import net.ntworld.mergeRequestIntegrationIde.infrastructure.internal.ApplicationSettingsImpl

interface SettingOption<T> {
    val name: String

    fun writeValue(value: T): String

    fun readValue(input: String, currentSettings: ApplicationSettingsImpl): ApplicationSettingsImpl
}
