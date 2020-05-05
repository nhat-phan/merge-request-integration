package net.ntworld.mergeRequestIntegrationIde.toolWindow

import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequestIntegrationIde.Component

interface ReworkToolWindowTab: Component {
    val providerData: ProviderData
}