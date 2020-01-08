package net.ntworld.mergeRequestIntegrationIde.ui.mergeRequest.tab.comment

import com.intellij.util.EventDispatcher
import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequest.MergeRequest
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequestIntegrationIde.service.CommentStore
import net.ntworld.mergeRequestIntegrationIde.ui.Component
import java.util.*

interface CommentDetailsUI : Component {
    val dispatcher: EventDispatcher<Listener>

    fun hide()

    fun displayComment(providerData: ProviderData, mergeRequest: MergeRequest, comment: Comment)

    fun showForm(providerData: ProviderData, mergeRequest: MergeRequest, comment: Comment?, item: CommentStore.Item)

    interface Listener : EventListener {
        fun onRefreshCommentsRequested(mergeRequest: MergeRequest)

        fun onReplyButtonClicked()
    }
}