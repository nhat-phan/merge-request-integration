package net.ntworld.mergeRequestIntegrationIde.infrastructure.option

import net.ntworld.mergeRequestIntegrationIde.infrastructure.internal.ApplicationSettingsImpl

class SaveMRFilterStateOption : BooleanOption() {
    override val name: String = "save-mr-filter-state"

    override fun getOptionValueFromSettings(settings: ApplicationSettingsImpl): Boolean {
        return settings.saveMRFilterState
    }

    override fun copySettings(settings: ApplicationSettingsImpl, value: Boolean): ApplicationSettingsImpl {
        return settings.copy(saveMRFilterState = value)
    }
}