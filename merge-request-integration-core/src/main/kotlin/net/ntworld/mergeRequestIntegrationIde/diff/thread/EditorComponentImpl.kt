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
import java.awt.Dimension
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import javax.swing.BorderFactory
import javax.swing.JComponent
import javax.swing.JPanel

class EditorComponentImpl(
    private val ideaProject: IdeaProject,
    val indent: Int
) : EditorComponent {
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
        }

        override fun displayTextInToolbar() = true
    }
    private val myAddCommentAction = object : AnAction(
        "Add comment", "Add comment to the current position", null
    ) {
        override fun actionPerformed(e: AnActionEvent) {
        }

        override fun useSmallerFontForTextInToolbar(): Boolean = false
        override fun displayTextInToolbar() = true
    }

    init {
        myEditorTextField.text = "\n\n"

        myPanel.setContent(myEditorTextField)
        myPanel.toolbar = createToolbar()

        myPanel.border = BorderFactory.createMatteBorder(0, indent * 40 + 1, 1, 1, JBColor.border())
        myEditorTextField.addComponentListener(myComponentListener)
    }

    override val dispatcher = EventDispatcher.create(EditorComponent.Event::class.java)

    override var isVisible: Boolean
        get() = myPanel.isVisible
        set(value) {
            myPanel.isVisible = value
        }

    override fun createComponent(): JComponent = myPanel

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
}