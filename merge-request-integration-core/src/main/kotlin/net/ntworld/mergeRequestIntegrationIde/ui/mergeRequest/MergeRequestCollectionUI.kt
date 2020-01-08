package net.ntworld.mergeRequestIntegrationIde.ui.mergeRequest

import com.intellij.util.EventDispatcher
import net.ntworld.mergeRequest.api.MergeRequestOrdering
import net.ntworld.mergeRequest.query.GetMergeRequestFilter
import net.ntworld.mergeRequestIntegrationIde.ui.Component

interface MergeRequestCollectionUI : Component {
    val eventDispatcher: EventDispatcher<MergeRequestCollectionEventListener>

    fun setFilter(filter: GetMergeRequestFilter)

    fun setOrder(ordering: MergeRequestOrdering)

    fun fetchData()
}
