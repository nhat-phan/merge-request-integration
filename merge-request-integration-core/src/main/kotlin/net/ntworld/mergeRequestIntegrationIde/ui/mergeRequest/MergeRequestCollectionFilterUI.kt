package net.ntworld.mergeRequestIntegrationIde.ui.mergeRequest

import com.intellij.util.EventDispatcher
import net.ntworld.mergeRequestIntegrationIde.ui.Component

interface MergeRequestCollectionFilterUI : Component {
    val eventDispatcher: EventDispatcher<MergeRequestCollectionFilterEventListener>

}