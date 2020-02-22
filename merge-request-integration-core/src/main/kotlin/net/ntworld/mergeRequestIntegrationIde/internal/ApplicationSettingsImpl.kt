package net.ntworld.mergeRequestIntegrationIde.internal

import net.ntworld.mergeRequestIntegrationIde.service.ApplicationSettings

data class ApplicationSettingsImpl(
    override val enableRequestCache: Boolean,
    override val groupCommentsByThread: Boolean,
    override val displayCommentsInDiffView: Boolean,
    override val checkoutTargetBranch: Boolean
) : ApplicationSettings {
    companion object {
        val DEFAULT = ApplicationSettingsImpl(
            enableRequestCache = true,
            groupCommentsByThread = false,
            displayCommentsInDiffView = false,
            checkoutTargetBranch = true
        )
    }
}