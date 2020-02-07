package net.ntworld.mergeRequestIntegrationIde.ui.configuration

import net.ntworld.mergeRequest.Project
import net.ntworld.mergeRequest.api.ApiCredentials
import java.util.*

interface ProjectFinderUI {
    fun setEnabled(value: Boolean, credentials: ApiCredentials)

    fun setSelectedProject(project: Project?)

    fun getSelectedProjectId(): String

    fun addProjectChangedListener(listener: ProjectChangedListener)

    interface ProjectChangedListener : EventListener {
        fun projectChanged(projectId: String)
    }
}