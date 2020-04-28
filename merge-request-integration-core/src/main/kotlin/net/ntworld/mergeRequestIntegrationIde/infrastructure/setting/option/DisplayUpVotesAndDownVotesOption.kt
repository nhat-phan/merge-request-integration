package net.ntworld.mergeRequestIntegrationIde.infrastructure.setting.option

import net.ntworld.mergeRequestIntegrationIde.infrastructure.setting.ApplicationSettingsImpl

class DisplayUpVotesAndDownVotesOption : BooleanOption() {
    override val name: String = "merge-request:display-upvotes-downvotes"

    override fun getOptionValueFromSettings(settings: ApplicationSettingsImpl): Boolean {
        return settings.displayUpVotesAndDownVotes
    }

    override fun copySettings(settings: ApplicationSettingsImpl, value: Boolean): ApplicationSettingsImpl {
        return settings.copy(displayUpVotesAndDownVotes = value)
    }
}