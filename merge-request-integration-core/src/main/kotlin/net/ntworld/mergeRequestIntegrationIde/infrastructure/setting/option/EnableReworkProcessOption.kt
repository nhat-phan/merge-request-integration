package net.ntworld.mergeRequestIntegrationIde.infrastructure.setting.option

import net.ntworld.mergeRequestIntegrationIde.infrastructure.setting.ApplicationSettingsImpl

class EnableReworkProcessOption : BooleanOption() {
    override val name: String = "rework:enabled"

    override fun getOptionValueFromSettings(settings: ApplicationSettingsImpl): Boolean {
        return settings.enableReworkProcess
    }

    override fun copySettings(settings: ApplicationSettingsImpl, value: Boolean): ApplicationSettingsImpl {
        return settings.copy(enableReworkProcess = value)
    }
}