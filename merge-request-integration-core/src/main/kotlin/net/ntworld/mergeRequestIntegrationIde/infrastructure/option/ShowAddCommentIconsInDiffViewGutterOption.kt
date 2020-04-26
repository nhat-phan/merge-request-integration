package net.ntworld.mergeRequestIntegrationIde.infrastructure.option

import net.ntworld.mergeRequestIntegrationIde.infrastructure.internal.ApplicationSettingsImpl

class ShowAddCommentIconsInDiffViewGutterOption : BooleanOption() {
    override val name: String = "comments:show-add-comment-icons-in-diff-view-gutter"

    override fun getOptionValueFromSettings(settings: ApplicationSettingsImpl): Boolean {
        return settings.showAddCommentIconsInDiffViewGutter
    }

    override fun copySettings(settings: ApplicationSettingsImpl, value: Boolean): ApplicationSettingsImpl {
        return settings.copy(showAddCommentIconsInDiffViewGutter = value)
    }
}