package net.ntworld.mergeRequestIntegrationIdeEE

import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.project.Project
import net.ntworld.mergeRequestIntegrationIde.ui.configuration.GithubConnectionsConfigurableBase

class GithubConnectionsConfigurable(project: Project) : GithubConnectionsConfigurableBase(
    ServiceManager.getService(project, EnterpriseProjectServiceProvider::class.java)
) {
    override fun getId(): String = "MRI:github-ee"

    override fun getDisplayName(): String = "Github"
}