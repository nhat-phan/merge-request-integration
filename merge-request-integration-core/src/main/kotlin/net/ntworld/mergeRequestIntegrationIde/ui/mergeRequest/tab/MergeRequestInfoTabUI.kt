package net.ntworld.mergeRequestIntegrationIde.ui.mergeRequest.tab

import net.ntworld.mergeRequest.MergeRequest
import net.ntworld.mergeRequest.MergeRequestInfo
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequestIntegrationIde.ui.Component

interface MergeRequestInfoTabUI : Component {
    fun setMergeRequestInfo(providerData: ProviderData, mr: MergeRequestInfo)

    fun setMergeRequest(mr: MergeRequest)
}