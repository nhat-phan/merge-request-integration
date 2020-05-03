package net.ntworld.mergeRequestIntegrationIde.infrastructure.notifier

import com.intellij.openapi.vcs.changes.Change
import com.intellij.util.messages.Topic
import net.ntworld.mergeRequest.ProviderData

interface SingleMRToolWindowNotifier {
    companion object {
        val TOPIC = Topic.create("MRI:SingleMRToolWindowNotifier", SingleMRToolWindowNotifier::class.java)
    }

    fun requestShowChanges(providerData: ProviderData, changes: List<Change>)

    fun requestHideChanges()
}