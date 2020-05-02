package net.ntworld.mergeRequestIntegrationIde.infrastructure.notifier

import com.intellij.openapi.vcs.changes.Change
import com.intellij.util.messages.Topic

interface ChangesToolWindowNotifier {
    companion object {
        val TOPIC = Topic.create("MRI:ChangesToolWindowNotifier", ChangesToolWindowNotifier::class.java)
    }

    fun requestOpenToolWindow()

    fun requestHideToolWindow()

    fun requestShowChanges(changes: List<Change>)

    fun requestHideChanges()
}