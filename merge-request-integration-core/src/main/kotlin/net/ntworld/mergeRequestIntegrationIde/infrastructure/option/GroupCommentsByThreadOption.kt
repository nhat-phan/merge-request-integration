package net.ntworld.mergeRequestIntegrationIde.infrastructure.option

import net.ntworld.mergeRequestIntegrationIde.infrastructure.internal.ApplicationSettingsImpl

class GroupCommentsByThreadOption : BooleanOption() {
    override val name: String = "comments:group-comment-by-thread"

    override fun getOptionValueFromSettings(settings: ApplicationSettingsImpl): Boolean {
        return settings.groupCommentsByThread
    }

    override fun copySettings(settings: ApplicationSettingsImpl, value: Boolean): ApplicationSettingsImpl {
        return settings.copy(groupCommentsByThread = value)
    }
}