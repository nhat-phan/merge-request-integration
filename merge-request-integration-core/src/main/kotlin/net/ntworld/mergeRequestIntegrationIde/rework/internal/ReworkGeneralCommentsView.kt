package net.ntworld.mergeRequestIntegrationIde.rework.internal

import com.intellij.openapi.ui.DialogBuilder
import com.intellij.openapi.ui.Messages
import com.intellij.ui.JBColor
import com.intellij.ui.ScrollPaneFactory
import com.intellij.ui.components.Panel
import com.intellij.util.ui.JBUI
import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequestIntegrationIde.component.comment.EditorComponent
import net.ntworld.mergeRequestIntegrationIde.component.comment.GroupComponent
import net.ntworld.mergeRequestIntegrationIde.component.comment.Options
import net.ntworld.mergeRequestIntegrationIde.infrastructure.ProjectServiceProvider
import net.ntworld.mergeRequestIntegrationIde.infrastructure.notifier.ReworkWatcherNotifier
import net.ntworld.mergeRequestIntegrationIde.rework.ReworkWatcher
import java.awt.Dimension
import javax.swing.BorderFactory
import javax.swing.BoxLayout
import javax.swing.ScrollPaneConstants

class ReworkGeneralCommentsView(
    private val projectServiceProvider: ProjectServiceProvider,
    private val providerData: ProviderData,
    private val reworkWatcher: ReworkWatcher
) {
    private val myReworkRequester = projectServiceProvider.messageBus.syncPublisher(ReworkWatcherNotifier.TOPIC)
    private val myGroupCommentCollection = mutableListOf<GroupComponent>()
    private val myThreadPanel = Panel()
    private val myThreadBoxLayout = JBUI.Panels.simplePanel()
    private val myThreadWrapper = ScrollPaneFactory.createScrollPane(myThreadBoxLayout, true)

    private val myMainEditorEventListener = object : EditorComponent.EventListener {
        override fun onEditorFocused(editor: EditorComponent) {
            myThreadWrapper.verticalScrollBar.value = myThreadWrapper.verticalScrollBar.maximum
        }

        override fun onEditorResized(editor: EditorComponent) {}

        override fun onCancelClicked(editor: EditorComponent) {
            if (editor.text.isNotBlank()) {
                val result = Messages.showYesNoDialog(
                    "Do you want to delete the whole content?", "Are you sure", Messages.getQuestionIcon()
                )
                result == Messages.YES
            }
            editor.text = ""
        }

        override fun onSubmitClicked(editor: EditorComponent, isDraft: Boolean) {
            val text = editor.text.trim()
            if (text.isNotEmpty()) {
                myReworkRequester.requestCreateComment(providerData, text, null, isDraft)
            }
        }
    }

    private val myMainEditor by lazy {
        val editor = projectServiceProvider.componentFactory.commentComponents.makeEditor(
            projectServiceProvider.project, EditorComponent.Type.NEW_DISCUSSION, 0,
            borderLeftRight = 0, showCancelAction = false
        )
        editor.isVisible = true
        editor.addListener(myMainEditorEventListener)

        editor
    }

    private val myGroupComponentEventListener = object : GroupComponent.EventListener {
        override fun onResized() {}

        override fun onOpenDialogClicked() {
        }

        override fun onEditorCreated(groupId: String, editor: EditorComponent) {
        }

        override fun onEditorDestroyed(groupId: String, editor: EditorComponent) {
        }

        override fun onReplyCommentRequested(comment: Comment, content: String) {
            if (content.trim().isNotEmpty()) {
                myReworkRequester.requestReplyComment(providerData, content, comment)
            }
        }

        override fun onEditCommentRequested(comment: Comment, content: String) {
            myReworkRequester.requestEditComment(providerData, comment, content)
        }

        override fun onPublishDraftCommentRequested(comment: Comment) {
            myReworkRequester.requestPublishComment(providerData, comment)
        }

        override fun onDeleteCommentRequested(comment: Comment) {
            myReworkRequester.requestDeleteComment(providerData, comment)
        }

        override fun onResolveCommentRequested(comment: Comment) {
            myReworkRequester.requestResolveComment(providerData, comment)
        }

        override fun onUnresolveCommentRequested(comment: Comment) {
            myReworkRequester.requestUnresolveComment(providerData, comment)
        }
    }

    init {
        myMainEditor.addCommentNowButtonText = "Add general comment"
        myMainEditor.addCommentNowButtonDesc = "Create new thread of general comment"

        myThreadPanel.background = JBColor.border()
        myThreadBoxLayout.background = JBColor.border()
        myThreadWrapper.background = JBColor.border()
        myThreadWrapper.border = BorderFactory.createMatteBorder(1, 1, 0, 1, JBColor.border())
        myThreadWrapper.minimumSize = Dimension(600, 100)
        myThreadWrapper.maximumSize = Dimension(1200, -1)
        myThreadWrapper.horizontalScrollBarPolicy = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER

        myThreadBoxLayout.addToCenter(myThreadPanel)
        myThreadPanel.layout = BoxLayout(myThreadPanel, BoxLayout.Y_AXIS)
        myThreadBoxLayout.addToBottom(myMainEditor.component)
    }

    fun focusMainEditor() {
        myMainEditor.focus()
    }

    fun render(groupedComments: Map<String, List<Comment>>) {
        myThreadPanel.removeAll()
        myGroupCommentCollection.forEach { it.dispose() }
        myGroupCommentCollection.clear()

        groupedComments.forEach { (groupId, comments) ->
            if (comments.isEmpty()) {
                return@forEach
            }

            val group = projectServiceProvider.componentFactory.commentComponents.makeGroup(
                reworkWatcher.providerData,
                reworkWatcher.mergeRequestInfo,
                projectServiceProvider.project,
                false, groupId, comments,
                Options(borderLeftRight = 0, showMoveToDialog = false)
            )

            group.addListener(myGroupComponentEventListener)
            myThreadPanel.add(group.component)
            myGroupCommentCollection.add(group)
        }

        val builder = DialogBuilder()
        builder.setCenterPanel(myThreadWrapper)
        builder.removeAllActions()
        builder.addOkAction()
        builder.resizable(true)
        builder.show()
    }
}