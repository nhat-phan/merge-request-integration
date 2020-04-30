package net.ntworld.mergeRequestIntegrationIdeEE

import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.project.Project
import net.ntworld.mergeRequestIntegrationIde.ui.configuration.GitlabConnectionsConfigurableBase

class GitlabConnectionsConfigurable(project: Project) : GitlabConnectionsConfigurableBase(
    ServiceManager.getService(project, EnterpriseProjectServiceProvider::class.java)
) {
    override fun getId(): String = "MRI:gitlab-ee"

    override fun getDisplayName(): String = "Gitlab"
}

