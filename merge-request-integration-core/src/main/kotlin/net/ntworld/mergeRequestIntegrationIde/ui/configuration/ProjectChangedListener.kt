package net.ntworld.mergeRequestIntegrationIde.ui.configuration

import java.util.*

interface ProjectChangedListener: EventListener {
    fun projectChanged(projectId: String)
}