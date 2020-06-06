package net.ntworld.mergeRequestIntegrationIde.infrastructure.notifier

import com.intellij.util.messages.Topic
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequestIntegrationIde.infrastructure.ReviewContext
import net.ntworld.mergeRequestIntegrationIde.infrastructure.setting.ApplicationSettings

interface ProjectNotifier {
    companion object {
        val TOPIC = Topic.create("MRI:ProjectNotifier", ProjectNotifier::class.java)
    }

    fun starting()

    fun initialized()

    fun providerRegistered(providerData: ProviderData)

    fun startCodeReview(reviewContext: ReviewContext)

    fun stopCodeReview(reviewContext: ReviewContext)
}