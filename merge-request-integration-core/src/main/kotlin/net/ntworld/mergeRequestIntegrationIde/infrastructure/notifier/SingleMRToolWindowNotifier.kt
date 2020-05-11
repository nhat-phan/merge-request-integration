package net.ntworld.mergeRequestIntegrationIde.infrastructure.notifier

import com.intellij.openapi.vcs.changes.Change
import com.intellij.util.messages.Topic
import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequest.MergeRequestInfo
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequestIntegrationIde.rework.ReworkWatcher

interface SingleMRToolWindowNotifier {
    companion object {
        val TOPIC = Topic.create("MRI:SingleMRToolWindowNotifier", SingleMRToolWindowNotifier::class.java)
    }

    fun registerReworkWatcher(reworkWatcher: ReworkWatcher)

    fun removeReworkWatcher(reworkWatcher: ReworkWatcher)

    fun hideChangesAfterDoingCodeReview()

    fun clearReworkTabs()

    fun showChangesWhenDoingCodeReview(providerData: ProviderData, changes: List<Change>)

    fun showReworkChanges(providerData: ProviderData, changes: List<Change>)

    fun showReworkComments(
        providerData: ProviderData,
        mergeRequestInfo: MergeRequestInfo,
        comments: List<Comment>,
        displayResolvedComments: Boolean
    )
}