package net.ntworld.mergeRequestIntegrationIde.infrastructure.setting

import net.ntworld.mergeRequest.api.ApiOptions
import net.ntworld.mergeRequestIntegration.internal.ApiOptionsImpl

interface ApplicationSettings {
    val enableRequestCache: Boolean

    val saveMRFilterState: Boolean

    val displayCommentsInDiffView: Boolean

    val showAddCommentIconsInDiffViewGutter: Boolean

    val checkoutTargetBranch: Boolean

    val maxDiffChangesOpenedAutomatically: Int

    val displayUpVotesAndDownVotes: Boolean

    val displayMergeRequestState: Boolean

    fun toApiOptions(): ApiOptions {
        return ApiOptionsImpl(
            enableRequestCache = this.enableRequestCache
        )
    }
}
