package net.ntworld.mergeRequestIntegrationIde.infrastructure.setting

data class ApplicationSettingsImpl(
    override val enableRequestCache: Boolean,
    override val saveMRFilterState: Boolean,
    override val displayCommentsInDiffView: Boolean,
    override val showAddCommentIconsInDiffViewGutter: Boolean,
    override val checkoutTargetBranch: Boolean,
    override val maxDiffChangesOpenedAutomatically: Int,
    override val displayUpVotesAndDownVotes: Boolean,
    override val displayMergeRequestState: Boolean
) : ApplicationSettings {
    companion object {
        val DEFAULT = ApplicationSettingsImpl(
            enableRequestCache = true,
            saveMRFilterState = true,
            displayCommentsInDiffView = false,
            showAddCommentIconsInDiffViewGutter = true,
            checkoutTargetBranch = false,
            maxDiffChangesOpenedAutomatically = 10,
            displayUpVotesAndDownVotes = false,
            displayMergeRequestState = true
        )
    }
}