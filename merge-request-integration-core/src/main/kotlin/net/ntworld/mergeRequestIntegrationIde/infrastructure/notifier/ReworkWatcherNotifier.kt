package net.ntworld.mergeRequestIntegrationIde.infrastructure.notifier

import com.intellij.util.messages.Topic
import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequestIntegrationIde.component.gutter.GutterPosition
import net.ntworld.mergeRequestIntegrationIde.mergeRequest.comments.tree.CommentTreeView
import net.ntworld.mergeRequestIntegrationIde.mergeRequest.comments.tree.node.Node

interface ReworkWatcherNotifier {
    companion object {
        val TOPIC = Topic.create("MRI:ReworkWatcherNotifier", ReworkWatcherNotifier::class.java)
    }

    fun requestFetchComment(providerData: ProviderData)

    fun changeDisplayResolvedComments(providerData: ProviderData, value: Boolean)

    fun commentTreeNodeSelected(providerData: ProviderData, node: Node, type: CommentTreeView.TreeSelectType)

    fun openCreateGeneralCommentForm(providerData: ProviderData)

    fun requestReplyComment(providerData: ProviderData, content: String, repliedComment: Comment)

    fun requestCreateComment(providerData: ProviderData, content: String, position: GutterPosition?)

    fun requestDeleteComment(providerData: ProviderData, comment: Comment)

    fun requestResolveComment(providerData: ProviderData, comment: Comment)

    fun requestUnresolveComment(providerData: ProviderData, comment: Comment)
}