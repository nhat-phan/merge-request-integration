package net.ntworld.mergeRequestIntegrationIde.diff

import com.intellij.diff.util.Side
import com.intellij.openapi.util.Key
import com.intellij.openapi.vcs.changes.Change
import com.intellij.util.messages.Topic
import net.ntworld.mergeRequestIntegrationIde.infrastructure.ReviewContext

interface DiffNotifier {
    companion object {
        val TOPIC = Topic.create("MRI:DiffNotifier", DiffNotifier::class.java)

        val ScrollLine = Key.create<Int>("DiffNotifier.ScrollLine")
        val ScrollSide = Key.create<Side>("DiffNotifier.ScrollSide")
        val ScrollShowComments = Key.create<Boolean>("DiffNotifier.ScrollShowComments")
    }

    fun scrollToLineRequested(
        reviewContext: ReviewContext,
        change: Change,
        line: Int,
        side: Side?,
        showComments: Boolean?
    )
}
