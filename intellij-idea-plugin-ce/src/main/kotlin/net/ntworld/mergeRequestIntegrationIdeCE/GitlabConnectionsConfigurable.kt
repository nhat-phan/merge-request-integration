package net.ntworld.mergeRequestIntegrationIdeCE

import com.intellij.openapi.project.Project
import net.ntworld.mergeRequestIntegrationIde.ui.configuration.gitlab.GitlabConnectionsConfigurableBase

class GitlabConnectionsConfigurable(myIdeaProject: Project) : GitlabConnectionsConfigurableBase(myIdeaProject) {
    override fun getId(): String {
        return "merge-request-integration-ce-gitlab-connections"
    }

    override fun getDisplayName(): String {
        return "Gitlab"
    }
}