package net.ntworld.mergeRequestIntegrationIdeCE

import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.project.Project
import net.ntworld.mergeRequestIntegrationIde.ui.configuration.GitlabConnectionsConfigurableBase

class GitlabConnectionsConfigurable(project: Project) : GitlabConnectionsConfigurableBase(
    ServiceManager.getService(project, CommunityProjectServiceProvider::class.java)
) {
    override fun getId(): String = "MRI:gitlab-ce"

    override fun getDisplayName(): String = "Gitlab"
}
