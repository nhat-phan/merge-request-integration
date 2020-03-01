package net.ntworld.mergeRequestIntegrationIde.internal.option

import net.ntworld.mergeRequestIntegrationIde.internal.ApplicationSettingsImpl

class SaveMRFilterStateOption : BooleanOption() {
    override val name: String = "save-mr-filter-state"

    override fun getOptionValueFromSettings(settings: ApplicationSettingsImpl): Boolean {
        return settings.saveMRFilterState
    }

    override fun copySettings(settings: ApplicationSettingsImpl, value: Boolean): ApplicationSettingsImpl {
        return settings.copy(saveMRFilterState = value)
    }
}