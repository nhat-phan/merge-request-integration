package net.ntworld.mergeRequestIntegrationIde.infrastructure.notifier

import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequestIntegrationIde.infrastructure.ReviewContext

open class ProjectNotifierAdapter : ProjectNotifier {
    override fun starting() {
    }

    override fun initialized() {
    }

    override fun providerRegistered(providerData: ProviderData) {
    }

    override fun startCodeReview(reviewContext: ReviewContext) {
    }

    override fun stopCodeReview(reviewContext: ReviewContext) {
    }
}