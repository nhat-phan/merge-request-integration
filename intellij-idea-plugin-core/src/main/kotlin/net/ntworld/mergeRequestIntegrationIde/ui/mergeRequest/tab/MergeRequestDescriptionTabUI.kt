package net.ntworld.mergeRequestIntegrationIde.ui.mergeRequest.tab

import net.ntworld.mergeRequest.MergeRequestInfo
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequestIntegrationIde.ui.Component

interface MergeRequestDescriptionTabUI: Component {
    fun setMergeRequestInfo(providerData: ProviderData, mr: MergeRequestInfo)
}