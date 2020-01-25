package net.ntworld.mergeRequestIntegrationIde.ui.configuration;

import com.intellij.openapi.ui.Messages
import com.intellij.util.EventDispatcher
import net.ntworld.mergeRequest.Project
import net.ntworld.mergeRequest.api.ApiConnection
import net.ntworld.mergeRequest.api.ApiCredentials
import net.ntworld.mergeRequestIntegrationIde.internal.ApiCredentialsImpl
import java.util.*
import javax.swing.*

class GitlabConnection : ConnectionUI {
    var myWholePanel: JPanel? = null
    var mySettingsPanel: JPanel? = null
    var myName: JTextField? = null
    var myUrl: JTextField? = null
    var myToken: JPasswordField? = null
    var myTestBtn: JButton? = null
    var myDeleteBtn: JButton? = null
    var myRepository: JComboBox<String>? = null
    var myTerm: JTextField? = null
    var myProjectList: JList<Project>? = null
    var myShared: JCheckBox? = null
    var mySearchStarred: JCheckBox? = null
    var mySearchMembership: JCheckBox? = null
    var mySearchOwn: JCheckBox? = null
    var myMergeApprovalsFeature: JCheckBox? = null
    var myIgnoreSSLError: JCheckBox? = null

    override val dispatcher = EventDispatcher.create(ConnectionUI.Listener::class.java)

    init {
        myTestBtn!!.addActionListener { onTestClicked() }
    }

    override fun initialize(name: String, credentials: ApiCredentials, shared: Boolean, repository: String) {
        myName!!.text = name
        myUrl!!.text = credentials.url
        myToken!!.text = credentials.token
        myIgnoreSSLError!!.isSelected = credentials.ignoreSSLCertificateErrors
    }

    override fun setName(name: String) {
        myName!!.text = name
    }

    override fun onConnectionTested(name: String, connection: ApiConnection, shared: Boolean) {
        Messages.showInfoMessage("Successfully connected!", "Info")
    }

    override fun onConnectionError(name: String, connection: ApiConnection, shared: Boolean, exception: Exception) {
        Messages.showErrorDialog("Cannot connect, error: ${exception.message}", "Error")
    }

    override fun onCredentialsVerified(name: String, credentials: ApiCredentials, repository: String) {

    }

    override fun onCredentialsInvalid(name: String, credentials: ApiCredentials, repository: String) {

    }

    override fun createComponent(): JComponent = myWholePanel!!

    private fun getToken(): String {
        return String(myToken!!.password)
    }

    private fun onTestClicked() {
        val name = myName!!.text
        val url = myUrl!!.text
        val token = getToken()
        val shared = myShared!!.isSelected
        val ignoreSSLCertificateErrors = myIgnoreSSLError!!.isSelected
        this.dispatcher.multicaster.test(
            this,
            name,
            ApiCredentialsImpl(
                url = url,
                login = "",
                token = token,
                ignoreSSLCertificateErrors = ignoreSSLCertificateErrors,
                info = "",
                projectId = "",
                version = ""
            ),
            shared
        )
    }
}
