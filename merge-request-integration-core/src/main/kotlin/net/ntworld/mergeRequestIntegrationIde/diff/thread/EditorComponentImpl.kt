package net.ntworld.mergeRequestIntegrationIde.diff.thread

import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.editor.impl.DocumentImpl
import com.intellij.openapi.project.Project as IdeaProject
import com.intellij.ui.EditorSettingsProvider
import com.intellij.ui.EditorTextField
import com.intellij.ui.JBColor
import com.intellij.util.EventDispatcher
import net.miginfocom.swing.MigLayout
import net.ntworld.mergeRequestIntegrationIde.ui.util.CustomSimpleToolWindowPanel
import net.ntworld.mergeRequestIntegrationIde.ui.util.FileTypeUtil
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.event.FocusEvent
import java.awt.event.FocusListener
import javax.swing.BorderFactory
import javax.swing.JComponent
import javax.swing.JPanel

class EditorComponentImpl(
    private val ideaProject: IdeaProject,
    private val type: EditorComponent.Type,
    val indent: Int
) : EditorComponent {
    private val dispatcher = EventDispatcher.create(EditorComponent.EventListener::class.java)
    private val myPanel = CustomSimpleToolWindowPanel(vertical = true, borderless = false)
    private val myDocument = DocumentImpl("")
    private val myEditorSettingsProvider = EditorSettingsProvider { editor ->
        if (null !== editor) {
            editor.settings.isLineNumbersShown = true
            editor.settings.isFoldingOutlineShown = true

            editor.setHorizontalScrollbarVisible(true)
            editor.setVerticalScrollbarVisible(true)
        }
    }
    private val myEditorTextField by lazy {
        val textField = EditorTextField(myDocument, ideaProject, FileTypeUtil.markdownFileType)
        textField.setOneLineMode(false)
        textField.addSettingsProvider(myEditorSettingsProvider)
        textField
    }
    private val myComponentListener = object: ComponentAdapter() {
        override fun componentResized(e: ComponentEvent?) {
            dispatcher.multicaster.onEditorResized()
        }
    }
    private val myCancelAction = object : AnAction(
        "Cancel", "Cancel and delete this comment", null
    ) {
        override fun actionPerformed(e: AnActionEvent) {
            dispatcher.multicaster.onCancelClicked(this@EditorComponentImpl)
        }

        override fun displayTextInToolbar() = true
    }
    private val myAddCommentAction = object : AnAction(
        "Add comment", "Add comment to the current position", null
    ) {
        override fun actionPerformed(e: AnActionEvent) {
        }

        override fun update(e: AnActionEvent) {
            when (type) {
                EditorComponent.Type.NEW_DISCUSSION -> {
                    e.presentation.text = "Add comment"
                    e.presentation.description = "Add comment to the current position"
                }
                EditorComponent.Type.REPLY -> {
                    e.presentation.text = "Reply"
                    e.presentation.description = "Reply to current discussion thread"
                }
            }
        }

        override fun useSmallerFontForTextInToolbar(): Boolean = false
        override fun displayTextInToolbar() = true
    }
    private val myEditorFocusListener = object: FocusListener {
        override fun focusLost(e: FocusEvent?) {
            val editor = myEditorTextField.editor
            if (null === editor) {
                return
            }
            if (myEditorTextField.text.isBlank() || myEditorTextField.text == EMPTY_TEXT_REPLACED) {
                myEditorTextField.text = ""
                myPanel.toolbar!!.isVisible = false
                dispatcher.multicaster.onEditorResized()
            }
        }

        override fun focusGained(e: FocusEvent?) {
            if (myEditorTextField.text.isBlank()) {
                myEditorTextField.text = EMPTY_TEXT_REPLACED
            }
            myPanel.toolbar!!.isVisible = true
            dispatcher.multicaster.onEditorResized()
        }
    }

    init {
        when(type) {
            EditorComponent.Type.NEW_DISCUSSION -> myEditorTextField.setPlaceholder("Start a new discussion...")
            EditorComponent.Type.REPLY -> myEditorTextField.setPlaceholder("Reply...")
        }

        myPanel.setContent(myEditorTextField)
        myPanel.toolbar = createToolbar()
        myPanel.toolbar!!.isVisible = false

        drawBorderTop(false)
        myEditorTextField.addComponentListener(myComponentListener)
        myEditorTextField.addFocusListener(myEditorFocusListener)
    }

    override val component: JComponent = myPanel

    override var text: String
        get() = myEditorTextField.text
        set(value) {
            myEditorTextField.text = value
        }

    override var isVisible: Boolean
        get() = myPanel.isVisible
        set(value) {
            myPanel.isVisible = value
        }

    override fun focus() {
        myEditorTextField.grabFocus()
    }

    override fun drawBorderTop(display: Boolean) {
        myPanel.border = BorderFactory.createMatteBorder(
            if (display) 1 else 0, indent * 40 + 1, 1, 1, JBColor.border()
        )
    }

    override fun addListener(listener: EditorComponent.EventListener) = dispatcher.addListener(listener)

    override fun dispose() {
        dispatcher.listeners.clear()
    }

    private fun createToolbar(): JComponent {
        val panel = JPanel(MigLayout("ins 0, fill", "15[left]push[right]5", "center"))

        val leftActionGroup = DefaultActionGroup()
        leftActionGroup.add(myAddCommentAction)
        leftActionGroup.addSeparator()
        leftActionGroup.add(myCancelAction)

        val leftToolbar = ActionManager.getInstance().createActionToolbar(
            "${CommentComponentImpl::class.java.canonicalName}/toolbar-left",
            leftActionGroup,
            true
        )

        val rightActionGroup = DefaultActionGroup()
        val rightToolbar = ActionManager.getInstance().createActionToolbar(
            "${CommentComponentImpl::class.java.canonicalName}/toolbar-left",
            rightActionGroup,
            true
        )

        panel.add(leftToolbar.component)
        panel.add(rightToolbar.component)
        return panel
    }

    companion object {
        const val EMPTY_TEXT_REPLACED = "\n"
    }
}