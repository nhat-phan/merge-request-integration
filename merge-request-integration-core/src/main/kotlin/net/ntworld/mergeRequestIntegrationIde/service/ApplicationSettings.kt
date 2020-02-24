package net.ntworld.mergeRequestIntegrationIde.service

import net.ntworld.mergeRequest.api.ApiOptions
import net.ntworld.mergeRequestIntegration.internal.ApiOptionsImpl

interface ApplicationSettings {
    val enableRequestCache: Boolean

    val groupCommentsByThread: Boolean

    val displayCommentsInDiffView: Boolean

    val checkoutTargetBranch: Boolean

    val maxDiffChangesOpenedAutomatically: Int

    fun toApiOptions(): ApiOptions {
        return ApiOptionsImpl(
            enableRequestCache = this.enableRequestCache
        )
    }
}
