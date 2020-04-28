package net.ntworld.mergeRequestIntegrationIde.infrastructure.setting.option

import net.ntworld.mergeRequestIntegrationIde.infrastructure.setting.ApplicationSettingsImpl

class DisplayMergeRequestStateOption : BooleanOption() {
    override val name: String = "merge-request:display-state"

    override fun getOptionValueFromSettings(settings: ApplicationSettingsImpl): Boolean {
        return settings.displayMergeRequestState
    }

    override fun copySettings(settings: ApplicationSettingsImpl, value: Boolean): ApplicationSettingsImpl {
        return settings.copy(displayMergeRequestState = value)
    }
}