package net.ntworld.mergeRequestIntegrationIdeEE

import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.project.Project
import net.ntworld.mergeRequestIntegrationIde.ui.configuration.GithubConnectionsConfigurableBase

class GithubConnectionsConfigurable(myIdeaProject: Project) : GithubConnectionsConfigurableBase(
    ServiceManager.getService(EnterpriseApplicationService::class.java),
    myIdeaProject
) {
    override fun getId(): String = "MRI:github-ee"

    override fun getDisplayName(): String = "Github"
}