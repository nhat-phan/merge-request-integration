package net.ntworld.mergeRequestIntegrationIde.infrastructure.notifier

import com.intellij.util.messages.Topic
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequestIntegrationIde.mergeRequest.comments.tree.node.Node

interface ReworkWatcherNotifier {
    companion object {
        val TOPIC = Topic.create("MRI:ReworkWatcherNotifier", ReworkWatcherNotifier::class.java)
    }

    fun requestFetchComment(providerData: ProviderData)

    fun changeDisplayResolvedComments(providerData: ProviderData, value: Boolean)

    fun commentTreeNodeSelected(providerData: ProviderData, node: Node)

    fun openCreateGeneralCommentForm(providerData: ProviderData)
}