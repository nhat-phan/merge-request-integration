package net.ntworld.mergeRequestIntegrationIde.ui.provider

import com.intellij.util.EventDispatcher
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequestIntegrationIde.ui.Component

interface ProviderCollectionListUI: Component {

    val eventDispatcher: EventDispatcher<ProviderCollectionListEventListener>

    fun clear()

    fun addProvider(providerData: ProviderData)
}