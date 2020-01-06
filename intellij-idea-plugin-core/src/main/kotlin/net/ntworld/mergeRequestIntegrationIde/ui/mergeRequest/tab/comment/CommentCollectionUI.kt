package net.ntworld.mergeRequestIntegrationIde.ui.mergeRequest.tab.comment

import com.intellij.util.EventDispatcher
import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequest.MergeRequest
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequestIntegrationIde.service.CommentStore
import net.ntworld.mergeRequestIntegrationIde.ui.Component
import java.util.*

interface CommentCollectionUI : Component {
    val dispatcher: EventDispatcher<Listener>

    fun setComments(providerData: ProviderData, mergeRequest: MergeRequest, comments: List<Comment>)

    fun createReplyComment()

    interface Listener : EventListener {
        fun commentUnselected() {}

        fun commentSelected(providerData: ProviderData, mergeRequest: MergeRequest, comment: Comment) {}

        fun editorSelected(providerData: ProviderData, mergeRequest: MergeRequest, comment: Comment?, item: CommentStore.Item) {}

        fun commentsDisplayed(total: Int) {}

        fun refreshRequested(mergeRequest: MergeRequest) {}
    }
}