package net.ntworld.mergeRequestIntegrationIde.ui.mergeRequest

import net.ntworld.mergeRequest.api.MergeRequestOrdering
import net.ntworld.mergeRequest.query.GetMergeRequestFilter
import java.util.*

interface MergeRequestCollectionFilterEventListener: EventListener {

    fun filterChanged(filter: GetMergeRequestFilter)

    fun orderChanged(order: MergeRequestOrdering)

}