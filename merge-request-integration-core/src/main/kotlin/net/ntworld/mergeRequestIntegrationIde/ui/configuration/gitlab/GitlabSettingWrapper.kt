package net.ntworld.mergeRequestIntegrationIde.ui.configuration

import com.intellij.openapi.project.Project as IdeaProject
import com.intellij.openapi.ui.Messages
import com.intellij.util.EventDispatcher
import net.ntworld.mergeRequestIntegration.provider.gitlab.Gitlab
import net.ntworld.mergeRequestIntegrationIde.ui.configuration.gitlab.GitlabConnectionVerifier
import net.ntworld.mergeRequestIntegrationIde.internal.ProviderSettingsImpl
import net.ntworld.mergeRequestIntegrationIde.service.ApplicationService
import net.ntworld.mergeRequestIntegrationIde.service.ProjectService
import net.ntworld.mergeRequestIntegrationIde.service.ProviderSettings
import net.ntworld.mergeRequestIntegrationIde.ui.configuration.legacy.ConnectionConfigActionListener
import net.ntworld.mergeRequestIntegrationIde.ui.configuration.legacy.ConnectionConfigurationListener
import net.ntworld.mergeRequestIntegrationIde.ui.configuration.legacy.ConnectionValidator
import javax.swing.*

class GitlabSettingWrapper(private val myIdeaProject: IdeaProject, private val mySavedConnections: List<ProviderSettings>) {
    var myTabbedPane: JTabbedPane? = null
    var myWrapper: JPanel? = null
    private val myConnections: MutableList<ProviderSettings?> = mutableListOf()
    private val myTabActionListener = MyTabActionListener(this)
    private val myDispatcher = EventDispatcher.create(ConnectionConfigurationListener::class.java)

    fun getComponent(): JPanel {
        if (mySavedConnections.isEmpty()) {
            makeNewConnectionConfiguration("Default")
        } else {
            mySavedConnections.forEach {
                makeConnectionFromData(GitlabSettingPanel.findNameFromId(it.id), it)
            }
        }

        return myWrapper!!
    }

    private fun makeNewConnectionConfiguration(name: String) {
        val data = ProviderSettingsImpl.makeDefault(GitlabSettingPanel.findIdByName(name), Gitlab)
        makeConnectionFromData(name, data)
    }

    private fun makeConnectionFromData(name: String, settings: ProviderSettings) {
        myConnections.add(settings)

        val tab = GitlabSettingPanel(
            myIdeaProject,
            myConnections.lastIndex,
            settings,
            settings.sharable,
            myTabActionListener,
            myDispatcher
        )
        myTabbedPane!!.addTab(name, tab.getComponent())
    }

    private class MyTabActionListener(private val self: GitlabSettingWrapper) :
        ConnectionConfigActionListener {
        override fun initialized(index: Int, providerSettings: ProviderSettings, isShared: Boolean) {
            val valid = this.validate(index, providerSettings, isShared)
            if (valid && providerSettings.credentials.projectId.isNotEmpty()) {
                val pair = GitlabConnectionVerifier.verifyProject(providerSettings)
                self.myDispatcher.multicaster.connectionVerified(index, providerSettings, pair.first, pair.second)
            }
        }

        override fun idChanged(
            index: Int,
            oldId: String,
            providerSettings: ProviderSettings,
            isVerified: Boolean,
            isShared: Boolean
        ) {
            ProjectService.getInstance(self.myIdeaProject).removeProviderConfiguration(id = oldId)
            if (canSaveToProjectStore(providerSettings)) {
                saveToProjectStore(index, providerSettings)
            }
        }

        override fun fieldsChanged(index: Int, providerSettings: ProviderSettings, isVerified: Boolean, isShared: Boolean) {
            if (self.myTabbedPane!!.tabCount <= index) {
                return
            }
            self.myTabbedPane!!.setTitleAt(index, GitlabSettingPanel.findNameFromId(providerSettings.id))
            self.myConnections[index] = providerSettings
            val valid = this.validate(index, providerSettings, isShared)
            if (valid && isVerified) {
                if (isShared) {
                    this.saveToApplicationStore(providerSettings)
                }
                if (canSaveToProjectStore(providerSettings)) {
                    saveToProjectStore(index, providerSettings)
                }
            }
        }

        override fun testClicked(index: Int, providerSettings: ProviderSettings, isShared: Boolean) {
            val response = GitlabConnectionVerifier.verify(providerSettings)
            if (!response.first) {
                Messages.showErrorDialog("Cannot connect, error: ${response.second}", "Error")
            } else {
                Messages.showInfoMessage("Successfully connected!", "Info")
                if (isShared) {
                    saveToApplicationStore(providerSettings)
                }
            }
            self.myDispatcher.multicaster.connectionVerified(index, providerSettings, response.first, null)
        }

        override fun deleteClicked(index: Int, providerSettings: ProviderSettings, isShared: Boolean) {
            self.myConnections[index] = null
            self.myTabbedPane!!.setTitleAt(index, "(deleted)")
            self.myTabbedPane!!.setComponentAt(index, JPanel())
            ProjectService.getInstance(self.myIdeaProject).removeProviderConfiguration(id = providerSettings.id)
        }

        override fun createNewClicked() {
            self.makeNewConnectionConfiguration("Connection ${self.myTabbedPane!!.tabCount}")
        }

        private fun canSaveToProjectStore(providerSettings: ProviderSettings): Boolean {
            return providerSettings.repository.isNotEmpty() && providerSettings.credentials.projectId.isNotEmpty()
        }

        private fun saveToProjectStore(index: Int, providerSettings: ProviderSettings) {
            ProjectService.getInstance(self.myIdeaProject).addProviderConfiguration(
                id = providerSettings.id,
                info = providerSettings.info,
                credentials = providerSettings.credentials,
                repository = providerSettings.repository
            )
            self.myDispatcher.multicaster.connectionSaved(index, providerSettings)
        }

        private fun saveToApplicationStore(providerSettings: ProviderSettings) {
            ApplicationService.instance.addProviderConfiguration(
                id = providerSettings.id,
                info = providerSettings.info,
                credentials = providerSettings.credentials
            )
        }

        private fun validate(index: Int, providerSettings: ProviderSettings, isShared: Boolean): Boolean {
            val valid = ConnectionValidator.isValidForTesting(self.myConnections, index, providerSettings)
            self.myDispatcher.multicaster.connectionValidated(index, providerSettings, valid)
            return valid
        }
    }
}
