package net.ntworld.mergeRequestIntegrationIde.infrastructure.notifier

import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.util.messages.Topic
import net.ntworld.mergeRequest.MergeRequestInfo
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequestIntegrationIde.rework.ReworkWatcher

interface ReworkEditorNotifier {
    companion object {
        val TOPIC = Topic.create("MRI:ReworkEditorNotifier", ReworkEditorNotifier::class.java)
    }

    fun bootstrap(reworkWatcher: ReworkWatcher)

    fun bootstrap(editor: TextEditor, reworkWatcher: ReworkWatcher)

    fun open(providerData: ProviderData, path: String, line: Int? = null)

    fun commentsUpdated(providerData: ProviderData)

    fun shutdown(providerData: ProviderData)
}