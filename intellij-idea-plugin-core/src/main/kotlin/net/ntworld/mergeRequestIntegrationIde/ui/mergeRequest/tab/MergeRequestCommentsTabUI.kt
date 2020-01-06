package net.ntworld.mergeRequestIntegrationIde.ui.mergeRequest.tab

import com.intellij.util.EventDispatcher
import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequest.MergeRequest
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequestIntegrationIde.ui.Component
import java.util.*

interface MergeRequestCommentsTabUI : Component {
    val dispatcher: EventDispatcher<Listener>

    fun setComments(providerData: ProviderData, mergeRequest: MergeRequest, comments: List<Comment>)

    interface Listener : EventListener {
        fun commentsDisplayed(total: Int)

        fun refreshRequested(mergeRequest: MergeRequest)
    }
}