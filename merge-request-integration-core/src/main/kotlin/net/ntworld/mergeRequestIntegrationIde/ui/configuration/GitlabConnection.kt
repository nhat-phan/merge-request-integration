package net.ntworld.mergeRequestIntegrationIde.ui.configuration;

import com.intellij.util.EventDispatcher
import net.ntworld.mergeRequest.Project
import net.ntworld.mergeRequest.api.ApiConnection
import net.ntworld.mergeRequest.api.ApiCredentials
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

    override val dispatcher = EventDispatcher.create(EventListener::class.java)

    override fun setName(name: String) {
        myName!!.text = name
    }

    override fun onConnectionTested(name: String, connection: ApiConnection, shared: Boolean) {
        myName!!.text = name
        myUrl!!.text = connection.url
        myToken!!.text = connection.token
        myIgnoreSSLError!!.isSelected = connection.ignoreSSLCertificateErrors
    }

    override fun onConnectionError(name: String, connection: ApiConnection, shared: Boolean) {

    }

    override fun onCredentialsVerified(name: String, credentials: ApiCredentials, repository: String) {

    }

    override fun onCredentialsInvalid(name: String, credentials: ApiCredentials, repository: String) {

    }

    override fun createComponent(): JComponent = myWholePanel!!
}
