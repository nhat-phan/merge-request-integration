package net.ntworld.mergeRequestIntegrationIde.infrastructure.notifier

import com.intellij.util.messages.Topic
import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequest.MergeRequestInfo
import net.ntworld.mergeRequest.ProviderData

interface MergeRequestDataNotifier {
    companion object {
        val TOPIC = Topic.create("MRI:MergeRequestDataNotifier", MergeRequestDataNotifier::class.java)
    }

    fun fetchCommentsRequested(providerData: ProviderData, mergeRequestInfo: MergeRequestInfo)

    fun onCommentsUpdated(providerData: ProviderData, mergeRequestInfo: MergeRequestInfo, comments: List<Comment>)

}