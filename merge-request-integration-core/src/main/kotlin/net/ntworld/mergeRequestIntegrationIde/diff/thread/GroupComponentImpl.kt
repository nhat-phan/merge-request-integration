package net.ntworld.mergeRequestIntegrationIde.diff.thread

import com.intellij.openapi.ui.Messages
import com.intellij.openapi.project.Project as IdeaProject
import com.intellij.ui.JBColor
import com.intellij.ui.components.Panel
import com.intellij.util.EventDispatcher
import com.intellij.util.ui.JBUI
import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequest.MergeRequest
import net.ntworld.mergeRequest.ProviderData
import javax.swing.BorderFactory
import javax.swing.BoxLayout
import javax.swing.JComponent

class GroupComponentImpl(
    private val borderTop: Boolean,
    private val providerData: ProviderData,
    private val mergeRequest: MergeRequest,
    private val project: IdeaProject,
    override val id: String,
    override val comments: List<Comment>
) : GroupComponent {
    private val dispatcher = EventDispatcher.create(GroupComponent.EventListener::class.java)
    private val myBoxLayoutPanel = JBUI.Panels.simplePanel()
    private val myPanel = Panel()
    private var myEditor: EditorComponent? = null
    private var myEditorEventListener = object : EditorComponent.EventListener {
        override fun onEditorResized() {
            dispatcher.multicaster.onResized()
        }

        override fun onCancelClicked(editor: EditorComponent) {
            val shouldDestroy = if (editor.text.isNotBlank()) {
                val result = Messages.showYesNoDialog(
                    "Do you want to delete the whole content?", "Are you sure", Messages.getQuestionIcon()
                )
                result == Messages.YES
            } else true

            if (shouldDestroy) {
                destroyReplyEditor()
                dispatcher.multicaster.onResized()
            } else {
                editor.focus()
            }
        }
    }

    init {
        myPanel.layout = BoxLayout(myPanel, BoxLayout.Y_AXIS)
        if (borderTop) {
            myPanel.border = BorderFactory.createMatteBorder(1, 0, 0, 0, JBColor.border())
        }
        myBoxLayoutPanel.addToCenter(myPanel)

        comments.forEachIndexed { index, comment ->
            myPanel.add(
                ComponentFactory
                    .makeComment(this, providerData, mergeRequest, comment, if (index == 0) 0 else 1)
                    .component
            )
        }

    }

    override var collapse: Boolean = false
        set(value) {
            field = value
            if (value) {
                myPanel.components.forEachIndexed { index, component ->
                    if (index != 0) {
                        component.isVisible = false
                    }
                }
                val editor = myEditor
                if (null !== editor) {
                    editor.isVisible = false
                }
            } else {
                myPanel.components.forEachIndexed { index, component ->
                    if (index != 0) {
                        component.isVisible = true
                    }
                }
                val editor = myEditor
                if (null !== editor) {
                    editor.isVisible = true
                }
            }
            dispatcher.multicaster.onResized()
        }

    override fun requestDeleteComment(comment: Comment) {
        dispatcher.multicaster.onDeleteCommentRequested(comment)
    }

    override fun requestToggleResolvedStateOfComment(comment: Comment) {
        if (comment.resolved) {
            dispatcher.multicaster.onUnresolveCommentRequested(comment)
        } else {
            dispatcher.multicaster.onResolveCommentRequested(comment)
        }
    }

    override val component: JComponent = myBoxLayoutPanel

    override fun showReplyEditor() {
        val editor = myEditor
        if (null === editor) {
            val createdEditor = ComponentFactory.makeEditor(project, EditorComponent.Type.REPLY, 1)
            dispatcher.multicaster.onEditorCreated(this.id, createdEditor)
            createdEditor.addListener(myEditorEventListener)

            myBoxLayoutPanel.addToBottom(createdEditor.component)
            createdEditor.focus()
            myEditor = createdEditor
        } else {
            editor.focus()
        }
    }

    override fun destroyReplyEditor() {
        val editor = myEditor
        if (null !== editor) {
            editor.dispose()
            myBoxLayoutPanel.remove(editor.component)
            dispatcher.multicaster.onEditorDestroyed(this.id, editor)
            myEditor = null
        }
    }

    override fun addListener(listener: GroupComponent.EventListener) = dispatcher.addListener(listener)

    override fun dispose() {
        dispatcher.listeners.clear()
    }
}