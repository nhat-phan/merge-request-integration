package net.ntworld.mergeRequestIntegrationIde.ui.configuration

import com.intellij.openapi.Disposable
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.project.Project as IdeaProject
import net.ntworld.mergeRequest.ProviderInfo
import net.ntworld.mergeRequest.api.ApiCredentials
import net.ntworld.mergeRequestIntegrationIde.internal.ProviderSettingsImpl
import net.ntworld.mergeRequestIntegrationIde.service.ApplicationService
import net.ntworld.mergeRequestIntegrationIde.service.ProjectService
import net.ntworld.mergeRequestIntegrationIde.service.ProviderSettings
import net.ntworld.mergeRequestIntegrationIde.ui.util.Tabs
import net.ntworld.mergeRequestIntegrationIde.ui.util.TabsUI
import javax.swing.JComponent

abstract class AbstractConnectionsConfigurable(
    private val ideaProject: IdeaProject
) : SearchableConfigurable, Disposable {
    private val myData = mutableMapOf<String, MyProviderSettings>()
    private val myInitializedData: Map<String, MyProviderSettings> by lazy {
        val connections = mutableMapOf<String, MyProviderSettings>()
        val appConnections = ApplicationService.instance.getProviderConfigurations()
        ProjectService.getInstance(ideaProject).getProviderConfigurations().forEach {
            connections[it.id] = MyProviderSettings(
                id = it.id,
                info = it.info,
                credentials = it.credentials,
                repository = it.repository,
                sharable = it.sharable,
                deleted = false
            )
        }
        for (appConnection in appConnections) {
            if (connections.containsKey(appConnection.id)) {
                connections[appConnection.id] = MyProviderSettings(
                    id = connections[appConnection.id]!!.id,
                    info = connections[appConnection.id]!!.info,
                    credentials = connections[appConnection.id]!!.credentials,
                    repository = connections[appConnection.id]!!.repository,
                    sharable = true,
                    deleted = false
                )
            } else {
                connections[appConnection.id] = MyProviderSettings(
                    id = appConnection.id,
                    info = appConnection.info,
                    credentials = appConnection.credentials,
                    repository = "",
                    sharable = true,
                    deleted = false
                )
            }
        }
        connections
    }
    private val myTabs: TabsUI by lazy {
        Tabs(ideaProject, this)
    }

    init {
        myInitializedData.forEach { (key, value) ->
            initConnection(value)
            myData[key] = value
        }
    }

    abstract fun makeConnection(): ConnectionUI

    private fun initConnection(data: ProviderSettings): ConnectionUI {
        val ui = makeConnection()
        ui.onConnectionTested(data.id, data.credentials, data.sharable)
        if (data.repository.isNotEmpty()) {
            ui.onCredentialsVerified(data.id, data.credentials, data.repository)
        }
        return ui
    }

    override fun isModified(): Boolean {
        for (entry in myData) {
            if (!myInitializedData.containsKey(entry.key)) {
                return true
            }
            val initializedItem = myInitializedData[entry.key]
            if (null === initializedItem) {
                return true
            }
            if (!initializedItem.equals(entry.value)) {
                return true
            }
        }
        return false
    }

    override fun apply() {
    }

    override fun dispose() {
        myData.clear()
    }

    override fun createComponent(): JComponent? = myTabs.component

    private data class MyProviderSettings(
        override val id: String,
        override val info: ProviderInfo,
        override val credentials: ApiCredentials,
        override val repository: String,
        override val sharable: Boolean,
        val deleted: Boolean
    ): ProviderSettings
}