package net.ntworld.mergeRequestIntegrationIde.infrastructure.notifier

import com.intellij.openapi.vcs.changes.Change
import com.intellij.util.messages.Topic

interface SingleMRToolWindowNotifier {
    companion object {
        val TOPIC = Topic.create("MRI:SingleMRToolWindowNotifier", SingleMRToolWindowNotifier::class.java)
    }

    fun requestShowChanges(changes: List<Change>)

    fun requestHideChanges()
}