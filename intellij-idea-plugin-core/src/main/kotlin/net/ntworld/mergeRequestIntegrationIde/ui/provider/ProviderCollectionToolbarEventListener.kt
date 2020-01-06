package net.ntworld.mergeRequestIntegrationIde.ui.provider

import java.util.*

interface ProviderCollectionToolbarEventListener: EventListener {
    fun addClicked()

    fun refreshClicked()

    fun helpClicked()
}