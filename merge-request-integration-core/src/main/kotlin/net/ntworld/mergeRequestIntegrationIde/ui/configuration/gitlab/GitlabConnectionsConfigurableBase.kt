package net.ntworld.mergeRequestIntegrationIde.ui.configuration.gitlab

import com.intellij.openapi.Disposable
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.project.Project as IdeaProject
import net.ntworld.mergeRequestIntegrationIde.ui.configuration.GitlabSettingWrapper
import net.ntworld.mergeRequestIntegrationIde.internal.ProviderSettingsImpl
import net.ntworld.mergeRequestIntegrationIde.service.ApplicationService
import net.ntworld.mergeRequestIntegrationIde.service.ProjectService
import net.ntworld.mergeRequestIntegrationIde.service.ProviderSettings
import javax.swing.JComponent

abstract class GitlabConnectionsConfigurableBase(private val myIdeaProject: IdeaProject) : SearchableConfigurable, Disposable {

    override fun isModified(): Boolean {
        return false
    }


    override fun apply() {

    }

    override fun createComponent(): JComponent? {
        val connections = mutableMapOf<String, ProviderSettings>()
        val appConnections = ApplicationService.instance.getProviderConfigurations()
        ProjectService.getInstance(myIdeaProject).getProviderConfigurations().forEach {
            connections[it.id] = it
        }
        for (appConnection in appConnections) {
            if (connections.containsKey(appConnection.id)) {
                connections[appConnection.id] = ProviderSettingsImpl(
                    id = connections[appConnection.id]!!.id,
                    info = connections[appConnection.id]!!.info,
                    credentials = connections[appConnection.id]!!.credentials,
                    repository = connections[appConnection.id]!!.repository,
                    sharable = true
                )
            } else {
                connections[appConnection.id] = ProviderSettingsImpl(
                    id = appConnection.id,
                    info = appConnection.info,
                    credentials = appConnection.credentials,
                    repository = "",
                    sharable = true
                )
            }
        }
        return GitlabSettingWrapper(myIdeaProject, connections.values.toList()).getComponent()
    }

    override fun dispose() {
        // TODO: Add dispose
    }

}