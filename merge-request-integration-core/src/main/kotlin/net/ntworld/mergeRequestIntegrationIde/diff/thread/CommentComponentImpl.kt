package net.ntworld.mergeRequestIntegrationIde.diff.thread

import com.intellij.ide.util.TipUIUtil
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.ui.JBColor
import com.intellij.ui.components.Label
import net.miginfocom.swing.MigLayout
import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequestIntegrationIde.ui.mergeRequest.tab.MergeRequestDescriptionTab
import net.ntworld.mergeRequestIntegrationIde.ui.util.HtmlHelper
import java.awt.Color
import javax.swing.BorderFactory
import javax.swing.JComponent
import javax.swing.JPanel

class CommentComponentImpl(
    private val providerData: ProviderData,
    private val comment: Comment,
    private val indent: Int
) : CommentComponent {
    private val myPanel = SimpleToolWindowPanel(true, false)
    private val myNameLabel = Label(comment.author.name)
    private val myUsernameLabel = Label("@${comment.author.username}")
    private val myNameSeparatorLabel = Label("·")

    private val myWebView = TipUIUtil.createBrowser() as TipUIUtil.Browser
    private val myHtmlTemplate = MergeRequestDescriptionTab::class.java.getResource(
        "/templates/mr.comment.html"
    ).readText()
    private val myTimeAction = object : AnAction("3 days ago", null, null) {
        override fun actionPerformed(e: AnActionEvent) {
        }

        override fun useSmallerFontForTextInToolbar(): Boolean = false
        override fun displayTextInToolbar() = true
    }

    init {
        myUsernameLabel.foreground = Color(153, 153, 153)
        myWebView.text = buildHtml(providerData, comment)
        myPanel.toolbar = createToolbar()
        myPanel.setContent(myWebView.component)

        myPanel.border = BorderFactory.createMatteBorder(1, 1, 1, 1, JBColor.border())
    }

    override fun createComponent(): JComponent = myPanel

    private fun createToolbar(): JComponent {
        val panel = JPanel(MigLayout("ins 0, fill", "5[left]5[left]5[left]0[left, fill]push[right]", "center"))

        val leftActionGroup = DefaultActionGroup()
        leftActionGroup.add(myTimeAction)
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

        panel.add(myNameLabel)
        panel.add(myUsernameLabel)
        panel.add(myNameSeparatorLabel)
        panel.add(leftToolbar.component)
        panel.add(rightToolbar.component)
        return panel
    }

    private fun buildHtml(providerData: ProviderData, comment: Comment): String {
        val output = myHtmlTemplate
            .replace("{{content}}", HtmlHelper.convertFromMarkdown(comment.body))

        return HtmlHelper.resolveRelativePath(providerData, output)
    }
}