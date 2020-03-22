package net.ntworld.mergeRequestIntegrationIde.infrastructure.api

import com.intellij.util.messages.Topic
import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequest.MergeRequest
import net.ntworld.mergeRequest.ProviderData

interface CommentApiObserver {
    companion object {
        val TOPIC = Topic.create("MRI:CommentApiObserver", CommentApiObserver::class.java)
    }

    fun fetchCommentsRequested(providerData: ProviderData, mergeRequest: MergeRequest)

    fun onCommentsUpdated(providerData: ProviderData, mergeRequest: MergeRequest, comments: List<Comment>)
}