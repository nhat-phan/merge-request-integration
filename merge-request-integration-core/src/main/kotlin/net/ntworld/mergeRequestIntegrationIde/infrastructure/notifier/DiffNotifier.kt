package net.ntworld.mergeRequestIntegrationIde.infrastructure.notifier

import com.intellij.openapi.util.Key
import com.intellij.openapi.vcs.changes.Change
import com.intellij.util.messages.Topic
import net.ntworld.mergeRequest.CommentPosition
import net.ntworld.mergeRequestIntegrationIde.infrastructure.ReviewContext

interface DiffNotifier {
    companion object {
        val TOPIC = Topic.create("MRI:DiffNotifier", DiffNotifier::class.java)

        val ScrollPosition = Key.create<CommentPosition>("DiffNotifier.ScrollPosition")
        val ScrollShowComments = Key.create<Boolean>("DiffNotifier.ScrollShowComments")
    }

    fun scrollToPositionRequested(
        reviewContext: ReviewContext,
        change: Change,
        position: CommentPosition,
        showComments: Boolean?
    )

    fun hideAllCommentsRequested(reviewContext: ReviewContext, change: Change)
}
