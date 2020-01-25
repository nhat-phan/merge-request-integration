package net.ntworld.mergeRequestIntegrationIde.ui.configuration

import com.intellij.icons.AllIcons
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.ui.tabs.TabInfo
import com.intellij.openapi.project.Project as IdeaProject
import net.ntworld.mergeRequest.ProviderInfo
import net.ntworld.mergeRequest.api.ApiConnection
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
    private val myTabInfos = mutableMapOf<String, TabInfo>()
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
    private val myAddTabAction = object : AnAction(
        "New Connection", "Add new connection", AllIcons.Actions.OpenNewTab
    ) {
        override fun actionPerformed(e: AnActionEvent) {
            var count = myTabs.getTabs().tabCount
            var name = "Connection $count"
            while (myData.containsKey(findIdFromName(name))) {
                count++
                name = "Connection $count"
            }
            val data = ProviderSettingsImpl.makeDefault(findIdFromName(name), makeProviderInfo())
            val connection = makeConnection()
            connection.setName(name)
            addConnectionToTabPane(data, connection, true)
        }
    }

    init {
        myTabs.setCommonCenterActionGroupFactory {
            val actionGroup = DefaultActionGroup()
            actionGroup.add(myAddTabAction)
            actionGroup
        }

        myInitializedData.forEach { (key, value) ->
            val connectionUI = initConnection(value)
            addConnectionToTabPane(value, connectionUI)
            myData[key] = value
        }
    }

    abstract fun makeProviderInfo(): ProviderInfo

    abstract fun findNameFromId(id: String): String

    abstract fun findIdFromName(name: String): String

    abstract fun makeConnection(): ConnectionUI

    abstract fun validateConnection(connection: ApiConnection): Boolean

    private fun initConnection(data: ProviderSettings): ConnectionUI {
        val ui = makeConnection()
        ui.onConnectionTested(findNameFromId(data.id), data.credentials, data.sharable)
        if (data.repository.isNotEmpty()) {
            ui.onCredentialsVerified(findNameFromId(data.id), data.credentials, data.repository)
        }
        return ui
    }

    private fun addConnectionToTabPane(data: ProviderSettings, connectionUI: ConnectionUI, selected: Boolean = false) {
        val tabInfo = TabInfo(connectionUI.createComponent())
        tabInfo.text = findNameFromId(data.id)

        myTabInfos[data.id] = tabInfo
        myTabs.addTab(tabInfo)
        if (selected) {
            myTabs.getTabs().select(tabInfo, true)
        }
    }

    override fun isModified(): Boolean {
        val validData = myData.filter { validateProviderSettings(it.value) }
        if (validData.size != myInitializedData.size) {
            return true
        }

        for (entry in validData) {
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
        for (entry in myData) {
            if (entry.value.deleted) {
                println("Delete connection ${entry.key}")
                continue
            }

            if (validateProviderSettings(entry.value)) {
                println("Save connection ----------")
                println("id: ${entry.value.id}")
                println("projectId: ${entry.value.credentials.projectId}")
                println("url: ${entry.value.credentials.url}")
                println("login: ${entry.value.credentials.login}")
                println("token: ${entry.value.credentials.token}")
                println("ignoreSSLCertificateErrors: ${entry.value.credentials.ignoreSSLCertificateErrors}")
                println("repository: ${entry.value.repository}")
                println("--------------------------")
            }
        }
    }

    override fun dispose() {
        myData.clear()
        myTabInfos.clear()
    }

    override fun createComponent(): JComponent? = myTabs.component

    private fun validateProviderSettings(providerSettings: ProviderSettings): Boolean {
        return validateConnection(providerSettings.credentials) &&
            providerSettings.repository.isNotEmpty() &&
            providerSettings.credentials.projectId.isNotEmpty()
    }

    private data class MyProviderSettings(
        override val id: String,
        override val info: ProviderInfo,
        override val credentials: ApiCredentials,
        override val repository: String,
        override val sharable: Boolean,
        val deleted: Boolean
    ): ProviderSettings
}