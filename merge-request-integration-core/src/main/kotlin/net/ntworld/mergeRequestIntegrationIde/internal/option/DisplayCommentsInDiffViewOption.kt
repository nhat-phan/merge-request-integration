package net.ntworld.mergeRequestIntegrationIde.internal.option

import net.ntworld.mergeRequestIntegrationIde.internal.ApplicationSettingsImpl

object DisplayCommentsInDiffViewOption : SettingOption<Boolean> {
    override val name: String = "comments:display-comments-in-diff-view"

    override fun writeValue(value: Boolean): String {
        return if (value) "1" else "0"
    }

    override fun readValue(input: String, currentSettings: ApplicationSettingsImpl): ApplicationSettingsImpl {
        val value = input.trim() == "1"
        if (currentSettings.displayCommentsInDiffView != value) {
            return currentSettings.copy(displayCommentsInDiffView = value)
        }
        return currentSettings
    }
}