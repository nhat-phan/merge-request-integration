package net.ntworld.mergeRequestIntegrationIde.ui.panel

import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.editor.impl.DocumentImpl
import com.intellij.openapi.project.Project
import com.intellij.ui.EditorSettingsProvider
import com.intellij.ui.EditorTextField
import com.intellij.util.EventDispatcher
import net.ntworld.mergeRequest.*
import net.ntworld.mergeRequest.request.CreateCommentRequest
import net.ntworld.mergeRequest.request.ReplyCommentRequest
import net.ntworld.mergeRequestIntegration.internal.CommentPositionImpl
import net.ntworld.mergeRequestIntegration.make
import net.ntworld.mergeRequestIntegration.provider.ProviderException
import net.ntworld.mergeRequestIntegrationIde.service.ApplicationService
import net.ntworld.mergeRequestIntegrationIde.service.CommentStore
import net.ntworld.mergeRequestIntegrationIde.ui.Component
import net.ntworld.mergeRequestIntegrationIde.util.FileTypeUtil
import java.util.*
import javax.swing.*

class CommentEditorPanel(
    private val applicationService: ApplicationService,
    private val ideaProject: Project,
    private val providerData: ProviderData,
    private val mergeRequest: MergeRequest,
    private val comment: Comment?,
    private val item: CommentStore.Item
) : Component {
    var myWholePanel: JPanel? = null
    var myEditorWrapper: JPanel? = null
    var myDebugInfoWrapper: JPanel? = null
    var myAddDiffComment: JCheckBox? = null
    var myOkButton: JButton? = null
    var myCancelButton: JButton? = null
    var myOldHash: JTextField? = null
    var myOldLine: JTextField? = null
    var myOldPath: JLabel? = null
    var myNewHash: JTextField? = null
    var myNewLine: JTextField? = null
    var myNewPath: JLabel? = null

    private val dispatcher = EventDispatcher.create(Listener::class.java)

    private val myDocument = DocumentImpl("")
    private val myEditorSettingsProvider = object : EditorSettingsProvider {
        override fun customizeSettings(editor: EditorEx?) {
            if (null !== editor) {
                editor.settings.isLineNumbersShown = true
                editor.settings.isFoldingOutlineShown = true

                editor.setHorizontalScrollbarVisible(true)
                editor.setVerticalScrollbarVisible(true)
            }
        }
    }
    private val myEditorTextField by lazy {
        val textField = EditorTextField(myDocument, ideaProject, FileTypeUtil.markdownFileType)
        textField.setOneLineMode(false)
        textField.addSettingsProvider(myEditorSettingsProvider)
        textField
    }

    init {
        myEditorWrapper!!.add(myEditorTextField)
        myEditorTextField.text = item.body
        myOkButton!!.text = when (item.type) {
            CommentStore.ItemType.EDIT -> "Update"
            CommentStore.ItemType.NEW -> "Create"
            CommentStore.ItemType.REPLY -> "Reply"
        }
        when (item.type) {
            CommentStore.ItemType.EDIT -> initUpdateComment()
            CommentStore.ItemType.NEW -> {
                if (null === item.position) {
                    initCreateGeneralComment()
                } else {
                    initCreateComment()
                }
            }
            CommentStore.ItemType.REPLY -> initReplyComment()
        }
        myOkButton!!.addActionListener {
            when (item.type) {
                CommentStore.ItemType.EDIT -> updateComment()
                CommentStore.ItemType.NEW -> createComment()
                CommentStore.ItemType.REPLY -> replyComment()
            }
        }
        myCancelButton!!.addActionListener {
            dispatcher.multicaster.onCancelButtonClicked(providerData, mergeRequest, comment, item)
        }
        myAddDiffComment!!.addActionListener {
            myDebugInfoWrapper!!.isVisible = myAddDiffComment!!.isVisible && myAddDiffComment!!.isSelected
        }
    }

    private fun initUpdateComment() {}
    private fun updateComment() {}

    private fun initCreateGeneralComment() {
        myAddDiffComment!!.isVisible = false
        myAddDiffComment!!.isSelected = false
        myOkButton!!.text = "Create General Comment"
        hideCreateNewCommentComponents()
        hideDebugOfCreateNewCommentComponents()
    }

    private fun initCreateComment() {
        val position = item.position
        if (null !== position) {
            myAddDiffComment!!.isSelected = true
            myOldHash!!.text = position.baseHash
            myNewHash!!.text = position.headHash
            myNewLine!!.text = findLine(position.newLine)
            myOldLine!!.text = findLine(position.oldLine)
            myNewPath!!.text = if (null !== position.newPath) position.newPath else ""
            myOldPath!!.text = if (null !== position.oldPath) position.oldPath else ""
        }
        showCreateNewCommentComponents()
        hideDebugOfCreateNewCommentComponents()
    }

    private fun findLine(line: Int?): String {
        if (null === line) {
            return ""
        }
        return if (line > 0) line.toString() else ""
    }

    private fun createGeneralComment() {
        val response = applicationService.infrastructure.serviceBus() process CreateCommentRequest.make(
            providerId = providerData.id,
            mergeRequestId = mergeRequest.id,
            position = null,
            body = myEditorTextField.text
        ) ifError {
            applicationService.getProjectService(ideaProject).notify(
                "There was an error from server. \n\n ${it.message}",
                NotificationType.ERROR
            )
            throw ProviderException(it)
        }
        dispatcher.multicaster.onCommentCreated(providerData, mergeRequest, comment, item, response.createdCommentId)
    }

    private fun createCommentInPosition() {
        val position = item.position
        if (null !== position) {
            val response = applicationService.infrastructure.serviceBus() process CreateCommentRequest.make(
                providerId = providerData.id,
                mergeRequestId = mergeRequest.id,
                position = rebuildPosition(position),
                body = myEditorTextField.text
            ) ifError {
                applicationService.getProjectService(ideaProject).notify(
                    "There was an error from server. \n\n Please fill the line of old commit and new commit then try again.",
                    NotificationType.ERROR
                )
                showDebugOfCreateNewCommentComponents()
                throw ProviderException(it)
            }
            dispatcher.multicaster.onCommentCreated(providerData, mergeRequest, comment, item, response.createdCommentId)
        }
    }

    private fun createComment() {
        if (!myAddDiffComment!!.isSelected) {
            return createGeneralComment()
        }
        createCommentInPosition()
    }

    private fun rebuildPosition(currentPosition: CommentPosition): CommentPosition {
        val bashHash = myOldHash!!.text
        val headHash = myNewHash!!.text
        val oldLine = if (myOldLine!!.text.isEmpty()) null else myOldLine!!.text.toIntOrNull()
        val newLine = if (myNewLine!!.text.isEmpty()) null else myNewLine!!.text.toIntOrNull()
        val shouldRebuild = bashHash != currentPosition.baseHash ||
            headHash != currentPosition.headHash ||
            oldLine != currentPosition.oldLine ||
            newLine != currentPosition.newLine

        if (shouldRebuild) {
            return CommentPositionImpl(
                baseHash = bashHash,
                startHash = currentPosition.startHash,
                headHash = headHash,
                oldPath = currentPosition.oldPath,
                newPath = currentPosition.newPath,
                oldLine = oldLine,
                newLine = newLine,
                source = currentPosition.source,
                changeType = currentPosition.changeType
            )
        }
        return currentPosition
    }

    private fun initReplyComment() {
        hideCreateNewCommentComponents()
        hideDebugOfCreateNewCommentComponents()
    }

    private fun replyComment() {
        val repliedComment = comment
        if (null !== repliedComment) {
            val response = applicationService.infrastructure.serviceBus() process ReplyCommentRequest.make(
                providerId = providerData.id,
                mergeRequestId = mergeRequest.id,
                repliedComment = repliedComment,
                body = myEditorTextField.text
            ) ifError {
                applicationService.getProjectService(ideaProject).notify(
                    "There was an error from server. \n\n ${it.message}",
                    NotificationType.ERROR
                )
            }
            dispatcher.multicaster.onCommentCreated(providerData, mergeRequest, comment, item, response.createdCommentId)
        }
    }

    fun addDestroyListener(listener: Listener) {
        dispatcher.addListener(listener)
    }

    private fun showCreateNewCommentComponents() {
        myAddDiffComment!!.isVisible = true
    }

    private fun showDebugOfCreateNewCommentComponents() {
        myDebugInfoWrapper!!.isVisible = true
    }

    private fun hideCreateNewCommentComponents() {
        myAddDiffComment!!.isVisible = false
    }

    private fun hideDebugOfCreateNewCommentComponents() {
        myDebugInfoWrapper!!.isVisible = false
    }

    fun grabFocus() {
        ApplicationManager.getApplication().invokeLater {
            Thread.sleep(100)
            myEditorTextField.grabFocus()
        }
    }

    override fun createComponent(): JComponent = myWholePanel!!

    interface Listener : EventListener {
        fun onCancelButtonClicked(
            providerData: ProviderData,
            mergeRequest: MergeRequest,
            comment: Comment?,
            item: CommentStore.Item
        )

        fun onCommentCreated(
            providerData: ProviderData,
            mergeRequest: MergeRequest,
            comment: Comment?,
            item: CommentStore.Item,
            createdCommentId: String?
        )
    }
}
