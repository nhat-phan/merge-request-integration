package net.ntworld.mergeRequestIntegrationIde.ui.configuration

import com.intellij.util.EventDispatcher
import net.ntworld.mergeRequest.Project
import net.ntworld.mergeRequestIntegration.provider.gitlab.GitlabUtil
import net.ntworld.mergeRequestIntegrationIde.ui.configuration.gitlab.GitlabProjectFinder
import net.ntworld.mergeRequestIntegrationIde.internal.ApiCredentialsImpl
import net.ntworld.mergeRequestIntegrationIde.internal.ProviderSettingsImpl
import net.ntworld.mergeRequestIntegrationIde.service.ProviderSettings
import net.ntworld.mergeRequestIntegrationIde.util.RepositoryUtil
import java.awt.event.ActionEvent
import javax.swing.*
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import com.intellij.openapi.project.Project as IdeaProject

class GitlabSettingPanel(
    private val ideaProject: IdeaProject,
    index: Int,
    providerSettings: ProviderSettings,
    isShared: Boolean,
    private val myActionListener: ConnectionConfigActionListener,
    private val myDispatcher: EventDispatcher<ConnectionConfigurationListener>
) {
    var myWholePanel: JPanel? = null
    var mySettingsPanel: JPanel? = null
    var myName: JTextField? = null
    var myUrl: JTextField? = null
    var myToken: JPasswordField? = null
    var myTestBtn: JButton? = null
    var myCreateBtn: JButton? = null
    var myDeleteBtn: JButton? = null
    var myRepository: JComboBox<String>? = null
    var myTerm: JTextField? = null
    var myProjectList: JList<Project>? = null
    var myShared: JCheckBox? = null
    var mySearchStarred: JCheckBox? = null
    var mySearchMembership: JCheckBox? = null
    var mySearchOwn: JCheckBox? = null
    var myMergeApprovalsFeature: JCheckBox? = null

    private var myOldId: String = ""

    private val myProjectFinder: GitlabProjectFinder by lazy {
        GitlabProjectFinder(
            ideaProject,
            myTerm!!,
            myProjectList!!,
            mySearchStarred!!,
            mySearchMembership!!,
            mySearchOwn!!,
            myMergeApprovalsFeature!!
        )
    }

    private var myIsValid: Boolean = false
    private var mySelectedRepository: String? = null
    private var mySelectedProject: Project? = null
    private var myIsVerified: Boolean = false
    private var myIndex: Int = index
    private var myLockedUrl: String = ""
    private var myLockedToken: String = ""
    private val myInfo = providerSettings.info

    private val myNameDocumentListener = object: DocumentListener {
        override fun changedUpdate(e: DocumentEvent?) {
            val newId = findIdByName(myName!!.text)
            if (newId != myOldId) {
                checkLockedParams()
                if (myName!!.text.isNotEmpty()) {
                    myActionListener.idChanged(myIndex, myOldId, buildData(), myIsVerified, myShared!!.isSelected)
                }
            } else {
                checkLockedParams()
                if (myName!!.text.isNotEmpty()) {
                    myActionListener.fieldsChanged(myIndex, buildData(), myIsVerified, myShared!!.isSelected)
                }
            }
        }

        override fun insertUpdate(e: DocumentEvent?) = changedUpdate(e)

        override fun removeUpdate(e: DocumentEvent?) = changedUpdate(e)
    }

    private val myDocumentListener = object: DocumentListener {
        override fun changedUpdate(e: DocumentEvent?) {
            checkLockedParams()
            if (myName!!.text.isNotEmpty()) {
                myActionListener.fieldsChanged(myIndex, buildData(), myIsVerified, myShared!!.isSelected)
            }
        }

        override fun insertUpdate(e: DocumentEvent?) = changedUpdate(e)

        override fun removeUpdate(e: DocumentEvent?) = changedUpdate(e)
    }

    private val myConnectionConfigurationListener = object: ConnectionConfigurationListener {

        override fun connectionValidated(index: Int, providerSettings: ProviderSettings, isValid: Boolean) {
            if (index == myIndex) {
                myIsValid = isValid
                updateFieldsState()
            }
        }

        override fun connectionVerified(index: Int, providerSettings: ProviderSettings, success: Boolean, project: Project?) {
            if (index == myIndex) {
                myIsVerified = success
                mySelectedProject = project
                mySelectedRepository = providerSettings.repository
                if (success) {
                    lockVerifiedParams()
                }
                updateFieldsState()
            }
        }

        override fun connectionSaved(index: Int, providerSettings: ProviderSettings) {
            myOldId = providerSettings.id
        }

    }

    init {
        myName!!.text = findNameFromId(providerSettings.id)
        myUrl!!.text = if (providerSettings.credentials.url.isNotEmpty()) providerSettings.credentials.url else "https://gitlab.com"
        myToken!!.text = providerSettings.credentials.token
        myShared!!.isSelected = isShared
        myMergeApprovalsFeature!!.isSelected = GitlabUtil.hasMergeApprovalFeature(providerSettings.credentials)

        myName!!.document.addDocumentListener(myNameDocumentListener)
        myUrl!!.document.addDocumentListener(myDocumentListener)
        myToken!!.document.addDocumentListener(myDocumentListener)
        myShared!!.addActionListener(this::onFieldChanged)
        myRepository!!.addActionListener(this::onFieldChanged)
        myMergeApprovalsFeature!!.addChangeListener(object: ChangeListener {
            override fun stateChanged(e: ChangeEvent?) {
                myActionListener.fieldsChanged(myIndex, buildData(), myIsVerified, myShared!!.isSelected)
            }
        })
        myProjectFinder.addProjectChangedListener(object: ProjectChangedListener {
            override fun projectChanged(projectId: String) {
                myActionListener.fieldsChanged(myIndex, buildData(), myIsVerified, myShared!!.isSelected)
            }
        })

        myTestBtn!!.addActionListener { myActionListener.testClicked(myIndex, buildData(), myShared!!.isSelected) }
        myDeleteBtn!!.addActionListener { myActionListener.deleteClicked(myIndex, buildData(), myShared!!.isSelected) }
        myCreateBtn!!.addActionListener { myActionListener.createNewClicked() }

        myDispatcher.addListener(myConnectionConfigurationListener)

        myDeleteBtn!!.isVisible = myIndex != 0
        myActionListener.initialized(myIndex, providerSettings, isShared)
    }

    private fun buildData(): ProviderSettings {
        return ProviderSettingsImpl(
            id = findIdByName(myName!!.text.trim()),
            info = myInfo,
            credentials = ApiCredentialsImpl(
                url = myUrl!!.text.trim(),
                login = "",
                token = getToken().trim(),
                projectId = myProjectFinder.getSelectedProjectId(),
                version = "v4",
                info = if (myMergeApprovalsFeature!!.isSelected) GitlabUtil.getMergeApprovalFeatureInfo() else ""
            ),
            repository = myRepository!!.selectedItem as String? ?: ""
        )
    }

    fun getComponent(): JPanel = this.myWholePanel!!

    private fun getToken(): String {
        return String(myToken!!.password)
    }

    private fun checkLockedParams() {
        if (myLockedUrl != myUrl!!.text || myLockedToken != getToken()) {
            myIsVerified = false
        }
    }

    private fun lockVerifiedParams() {
        myLockedUrl = myUrl!!.text
        myLockedToken = getToken()
        val repositories = RepositoryUtil.getRepositoriesByProject(ideaProject)
        repositories.forEach { myRepository!!.addItem(it) }
        if (repositories.isNotEmpty()) {
            myRepository!!.selectedItem = repositories.first()
        }
    }

    private fun updateFieldsState() {
        myTestBtn!!.isEnabled = myIsValid
        myRepository!!.isEnabled = myIsValid && myIsVerified
        myProjectFinder.setEnabled(myIsValid && myIsVerified, ApiCredentialsImpl(
            url = myUrl!!.text.trim(),
            login = "",
            token = getToken().trim(),
            projectId = "",
            version = "v4",
            info = if (myMergeApprovalsFeature!!.isSelected) GitlabUtil.getMergeApprovalFeatureInfo() else ""
        ))
        if (null !== mySelectedRepository) {
            myRepository!!.selectedItem = mySelectedRepository
        }
        if (null !== mySelectedProject) {
            myProjectFinder.setSelectedProject(mySelectedProject)
        }
    }

    private fun onFieldChanged(e: ActionEvent) {
        myActionListener.fieldsChanged(myIndex, buildData(), myIsVerified, myShared!!.isSelected)
    }

    companion object {
        private const val PREFIX = "gitlab:"

        fun findNameFromId(id: String): String {
            if (id.startsWith(PREFIX)) {
                return id.substring(PREFIX.length)
            }
            return id
        }

        fun findIdByName(name: String): String {
            return "$PREFIX$name"
        }
    }
}
