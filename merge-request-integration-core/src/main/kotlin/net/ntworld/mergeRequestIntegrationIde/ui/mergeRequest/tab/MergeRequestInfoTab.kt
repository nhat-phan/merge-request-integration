package net.ntworld.mergeRequestIntegrationIde.ui.mergeRequest.tab

import net.ntworld.mergeRequest.MergeRequest
import net.ntworld.mergeRequest.MergeRequestInfo
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequestIntegrationIde.ui.panel.MergeRequestInfoPanel
import javax.swing.JComponent

class MergeRequestInfoTab : MergeRequestInfoTabUI {
    private val myInfoPanel = MergeRequestInfoPanel()

    override fun setMergeRequestInfo(providerData: ProviderData, mr: MergeRequestInfo) {
        myInfoPanel.setMergeRequestInfo(providerData, mr)
    }

    override fun setMergeRequest(mr: MergeRequest) = myInfoPanel.setMergeRequest(mr)

    override fun createComponent(): JComponent = myInfoPanel.createComponent()
}