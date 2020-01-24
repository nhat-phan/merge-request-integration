package net.ntworld.mergeRequestIntegrationIde.ui.configuration.legacy

import java.util.*

interface ProjectChangedListener: EventListener {
    fun projectChanged(projectId: String)
}