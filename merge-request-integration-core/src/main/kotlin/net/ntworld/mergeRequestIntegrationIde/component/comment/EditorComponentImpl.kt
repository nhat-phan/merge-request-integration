package net.ntworld.mergeRequestIntegrationIde.component.comment

import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.editor.impl.DocumentImpl
import com.intellij.openapi.project.Project as IdeaProject
import com.intellij.ui.EditorSettingsProvider
import com.intellij.ui.EditorTextField
import com.intellij.ui.JBColor
import com.intellij.util.EventDispatcher
import net.miginfocom.swing.MigLayout
import net.ntworld.mergeRequestIntegrationIde.ALLOW_DRAFT_COMMENTS
import net.ntworld.mergeRequestIntegrationIde.ui.util.CustomSimpleToolWindowPanel
import net.ntworld.mergeRequestIntegrationIde.util.FileTypeUtil
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
    val indent: Int,
    private val borderLeftRight: Int = 1,
    private val showCancelAction: Boolean = true
) : EditorComponent {
    private val dispatcher = EventDispatcher.create(EditorComponent.EventListener::class.java)
    private val myPanel = CustomSimpleToolWindowPanel(vertical = true, borderless = false)
    private val myDocument = DocumentImpl("")
    private val myEditorSettingsProvider = EditorSettingsProvider { editor ->
        if (null !== editor) {
            editor.settings.isLineNumbersShown = true
            editor.settings.isFoldingOutlineShown = true
            editor.settings.isUseSoftWraps = true

            editor.setHorizontalScrollbarVisible(false)
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
            dispatcher.multicaster.onEditorResized(this@EditorComponentImpl)
        }
    }

    private class MyCancelAction(private val self: EditorComponentImpl) : AnAction(
        "Cancel", "Cancel and delete this comment", null
    ) {
        override fun actionPerformed(e: AnActionEvent) {
            self.dispatcher.multicaster.onCancelClicked(self)
        }

        override fun displayTextInToolbar() = true
    }
    private val myCancelAction = MyCancelAction(this)

    private class MyAddCommentNowAction(private val self: EditorComponentImpl) : AnAction(
        "Add comment now", "Add comment to the current position", null
    ) {
        override fun actionPerformed(e: AnActionEvent) {
            if (self.myEditorTextField.text.trim().isNotBlank()) {
                self.dispatcher.multicaster.onSubmitClicked(self, false)
            }
        }

        override fun update(e: AnActionEvent) {
            when (self.type) {
                EditorComponent.Type.NEW_DISCUSSION -> {
                    e.presentation.text = self.addCommentNowButtonText
                    e.presentation.description = self.addCommentNowButtonDesc
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
    private class MyStartAReviewAction(private val self: EditorComponentImpl) : AnAction(
        "Start a review", "Save a comment for now then publish all comments later", null
    ) {
        override fun actionPerformed(e: AnActionEvent) {
            if (self.myEditorTextField.text.trim().isNotBlank()) {
                self.dispatcher.multicaster.onSubmitClicked(self, true)
            }
        }

        override fun update(e: AnActionEvent) {
            when (self.type) {
                EditorComponent.Type.NEW_DISCUSSION -> {
                    e.presentation.text = self.startAReviewButtonText
                    e.presentation.description = self.startAReviewButtonDesc
                    e.presentation.isVisible = true
                }
                EditorComponent.Type.REPLY -> {
                    e.presentation.isVisible = false
                }
            }
        }

        override fun useSmallerFontForTextInToolbar(): Boolean = false
        override fun displayTextInToolbar() = true
    }
    private val myAddCommentNowAction = MyAddCommentNowAction(this)
    private val myStartAReviewAction = MyStartAReviewAction(this)

    private val myEditorFocusListener = object: FocusListener {
        override fun focusLost(e: FocusEvent?) {
            val editor = myEditorTextField.editor
            if (null === editor) {
                return
            }
            if (myEditorTextField.text.isBlank() || myEditorTextField.text == EMPTY_TEXT_REPLACED) {
                myEditorTextField.text = ""
                myPanel.toolbar!!.isVisible = false
                dispatcher.multicaster.onEditorResized(this@EditorComponentImpl)
            }
        }

        override fun focusGained(e: FocusEvent?) {
            if (myEditorTextField.text.isBlank()) {
                myEditorTextField.text = EMPTY_TEXT_REPLACED
            }
            myPanel.toolbar!!.isVisible = true
            dispatcher.multicaster.onEditorResized(this@EditorComponentImpl)
            dispatcher.multicaster.onEditorFocused(this@EditorComponentImpl)
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

    override var addCommentNowButtonText: String = "Add comment now"
    override var addCommentNowButtonDesc: String = "Add comment to the current position"

    override var startAReviewButtonText: String = "Start a review"
    override var startAReviewButtonDesc: String = "Save a comment for now then publish all comments later"

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
            if (display) 1 else 0, indent * 40 + borderLeftRight, 1, borderLeftRight, JBColor.border()
        )
    }

    override fun addListener(listener: EditorComponent.EventListener) = dispatcher.addListener(listener)

    override fun dispose() {
        dispatcher.listeners.clear()
    }

    private fun createToolbar(): JComponent {
        val panel = JPanel(MigLayout("ins 0, fill", "15[left]push[right]5", "center"))

        val leftActionGroup = DefaultActionGroup()
        if (ALLOW_DRAFT_COMMENTS) {
            leftActionGroup.add(myStartAReviewAction)
            leftActionGroup.addSeparator()
        }
        leftActionGroup.add(myAddCommentNowAction)
        if (showCancelAction) {
            leftActionGroup.addSeparator()
            leftActionGroup.add(myCancelAction)
        }

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