package net.ntworld.mergeRequestIntegrationIde.ui.provider

import com.intellij.util.EventDispatcher
import net.ntworld.mergeRequestIntegrationIde.ui.Component
import net.ntworld.mergeRequestIntegrationIde.ui.mergeRequest.MergeRequestCollectionEventListener

interface ProviderDetailsUI : Component {
    val listEventDispatcher: EventDispatcher<MergeRequestCollectionEventListener>

    fun hide()

    fun show()
}