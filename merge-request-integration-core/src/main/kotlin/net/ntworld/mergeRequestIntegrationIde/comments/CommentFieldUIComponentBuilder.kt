package net.ntworld.mergeRequestIntegrationIde.comments

import com.intellij.ide.BrowserUtil
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.colors.EditorColors
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.editor.impl.EditorImpl
import com.intellij.openapi.editor.impl.view.FontLayoutService
import com.intellij.openapi.fileTypes.FileTypes
import com.intellij.openapi.keymap.KeymapUtil
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.ui.EditorTextField
import com.intellij.ui.ListFocusTraversalPolicy
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.labels.LinkLabel
import com.intellij.ui.components.panels.Wrapper
import com.intellij.util.ui.*
import com.intellij.util.ui.AATextInfo.putClientProperty
import net.miginfocom.layout.CC
import net.miginfocom.layout.LC
import net.miginfocom.swing.MigLayout
import java.awt.*
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.geom.RoundRectangle2D
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.ScrollPaneConstants
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

class CommentFieldUIComponentBuilder {

    fun build(project: Project): JComponent {
        val document = EditorFactory.getInstance().createDocument("SOME TEXT")
        val textField = object : EditorTextField(document, project, FileTypes.PLAIN_TEXT) {
            override fun createEditor(): EditorEx {
                return super.createEditor().apply {
                    component.isOpaque = false
                    scrollPane.isOpaque = false
                }
            }
        }.apply {
            putClientProperty(UIUtil.HIDE_EDITOR_FROM_DATA_CONTEXT_PROPERTY, true)
            setOneLineMode(false)
            setPlaceholder("Comment")
            addSettingsProvider {
                it.colorsScheme.lineSpacing = 1f
            }
        }

//        val button = JButton(actionName).apply {
//            isEnabled = false
//            isOpaque = false
//            toolTipText = KeymapUtil.getShortcutsText(submitShortcut.shortcuts)
//            putClientProperty(UIUtil.HIDE_EDITOR_FROM_DATA_CONTEXT_PROPERTY, true)
//        }
//        document.addDocumentListener(object : DocumentListener {
//            override fun documentChanged(event: DocumentEvent) {
//                button.isEnabled = document.immutableCharSequence.isNotBlank()
//            }
//        })

//        object : DumbAwareAction() {
//            override fun actionPerformed(e: AnActionEvent) = submit(project, button, textField, request)
//        }.registerCustomShortcutSet(submitShortcut, textField)
//        button.addActionListener {
//            submit(project, button, textField, request)
//        }

//        val authorLabel = LinkLabel.create("") {
//            BrowserUtil.browse(author.url)
//        }.apply {
//            icon = avatarIconsProvider.getIcon(author.avatarUrl)
//            isFocusable = true
//            border = JBUI.Borders.empty(if (UIUtil.isUnderDarcula()) 4 else 2, 0)
//            putClientProperty(UIUtil.HIDE_EDITOR_FROM_DATA_CONTEXT_PROPERTY, true)
//        }
//
//        document.addDocumentListener(object : DocumentListener {
//            override fun documentChanged(event: DocumentEvent) {
//                textField.revalidate()
//            }
//        })

        return JPanel().apply {
            isFocusCycleRoot = true
            isFocusTraversalPolicyProvider = true
            focusTraversalPolicy = ListFocusTraversalPolicy(listOf(textField
//                    button, authorLabel
            ))
            isOpaque = false
            layout = MigLayout(LC().gridGap("0", "0")
                    .insets("0", "0", "0", "0")
                    .fillX()).apply {
                columnConstraints = "[]${UI.scale(8)}[]${UI.scale(4)}[]"
            }
//            add(authorLabel, CC().alignY("top"))
            add(textField, CC().growX().pushX())
//            add(button, CC().newline().skip().alignX("left"))

            textField.isEnabled = true
        }
    }


}