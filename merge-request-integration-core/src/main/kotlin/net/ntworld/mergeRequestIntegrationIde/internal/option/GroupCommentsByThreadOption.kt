package net.ntworld.mergeRequestIntegrationIde.internal.option

import net.ntworld.mergeRequestIntegrationIde.internal.ApplicationSettingsImpl

object GroupCommentsByThreadOption : SettingOption<Boolean> {
    override val name: String = "comments:group-comment-by-thread"

    override fun writeValue(value: Boolean): String {
        return if (value) "1" else "0"
    }

    override fun readValue(input: String, currentSettings: ApplicationSettingsImpl): ApplicationSettingsImpl {
        val value = input.trim() == "1"
        if (currentSettings.groupCommentsByThread != value) {
            return currentSettings.copy(groupCommentsByThread = value)
        }
        return currentSettings
    }
}