package net.ntworld.mergeRequestIntegrationIde.ui.mergeRequest

import net.ntworld.mergeRequest.MergeRequest
import net.ntworld.mergeRequest.MergeRequestInfo
import net.ntworld.mergeRequestIntegrationIde.ui.Component

interface MergeRequestDetailsUI : Component {

    fun hide()

    fun setMergeRequest(mergeRequest: MergeRequest)

    fun setMergeRequestInfo(mergeRequestInfo: MergeRequestInfo)

}