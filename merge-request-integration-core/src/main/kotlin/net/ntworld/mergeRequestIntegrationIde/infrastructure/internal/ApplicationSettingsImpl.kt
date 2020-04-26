package net.ntworld.mergeRequestIntegrationIde.infrastructure.internal

import net.ntworld.mergeRequestIntegrationIde.infrastructure.ApplicationSettings

data class ApplicationSettingsImpl(
    override val enableRequestCache: Boolean,
    override val saveMRFilterState: Boolean,
    override val groupCommentsByThread: Boolean,
    override val displayCommentsInDiffView: Boolean,
    override val showAddCommentIconsInDiffViewGutter: Boolean,
    override val checkoutTargetBranch: Boolean,
    override val maxDiffChangesOpenedAutomatically: Int
) : ApplicationSettings {
    companion object {
        val DEFAULT = ApplicationSettingsImpl(
            enableRequestCache = true,
            saveMRFilterState = true,
            groupCommentsByThread = false,
            displayCommentsInDiffView = false,
            showAddCommentIconsInDiffViewGutter = true,
            checkoutTargetBranch = true,
            maxDiffChangesOpenedAutomatically = 10
        )
    }
}