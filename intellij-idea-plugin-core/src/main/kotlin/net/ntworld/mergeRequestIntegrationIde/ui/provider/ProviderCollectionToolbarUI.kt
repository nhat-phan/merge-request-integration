package net.ntworld.mergeRequestIntegrationIde.ui.provider

import com.intellij.util.EventDispatcher
import net.ntworld.mergeRequestIntegrationIde.ui.Component

interface ProviderCollectionToolbarUI : Component {
    val eventDispatcher: EventDispatcher<ProviderCollectionToolbarEventListener>
}