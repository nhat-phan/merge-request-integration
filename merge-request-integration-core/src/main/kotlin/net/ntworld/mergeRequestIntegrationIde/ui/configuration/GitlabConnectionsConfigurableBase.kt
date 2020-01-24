package net.ntworld.mergeRequestIntegrationIde.ui.configuration

import com.intellij.openapi.project.Project

open class GitlabConnectionsConfigurableBase(
    private val ideaProject: Project
) : AbstractConnectionsConfigurable(ideaProject) {
    override fun makeConnection(): ConnectionUI {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getId(): String = "MRI:gitlab"

    override fun getDisplayName(): String = "Gitlab new"
}