package net.ntworld.mergeRequestIntegrationIde.ui.provider

import net.ntworld.mergeRequest.ProviderData
import java.util.*

interface ProviderCollectionListEventListener : EventListener {

    fun providerUnselected()

    fun providerSelected(providerData: ProviderData)

    fun providerOpened(providerData: ProviderData)

}