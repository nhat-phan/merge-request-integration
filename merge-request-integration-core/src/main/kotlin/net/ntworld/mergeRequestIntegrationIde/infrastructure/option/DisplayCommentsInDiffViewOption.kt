package net.ntworld.mergeRequestIntegrationIde.infrastructure.option

import net.ntworld.mergeRequestIntegrationIde.infrastructure.internal.ApplicationSettingsImpl

class DisplayCommentsInDiffViewOption : BooleanOption() {
    override val name: String = "comments:display-comments-in-diff-view"

    override fun getOptionValueFromSettings(settings: ApplicationSettingsImpl): Boolean {
        return settings.displayCommentsInDiffView
    }

    override fun copySettings(settings: ApplicationSettingsImpl, value: Boolean): ApplicationSettingsImpl {
        return settings.copy(displayCommentsInDiffView = value)
    }
}