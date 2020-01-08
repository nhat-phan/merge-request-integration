package net.ntworld.mergeRequestIntegrationIde.ui.mergeRequest

import net.ntworld.mergeRequest.MergeRequestInfo
import net.ntworld.mergeRequest.ProviderData
import java.util.*

interface MergeRequestCollectionEventListener : EventListener {
    fun mergeRequestUnselected()

    fun mergeRequestSelected(providerData: ProviderData, mergeRequestInfo: MergeRequestInfo)
}