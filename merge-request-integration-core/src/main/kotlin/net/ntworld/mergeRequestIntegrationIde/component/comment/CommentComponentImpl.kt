package net.ntworld.mergeRequestIntegrationIde.component.comment

import com.intellij.ide.BrowserUtil
import com.intellij.ide.util.TipUIUtil
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.ui.JBColor
import com.intellij.ui.components.Label
import net.miginfocom.swing.MigLayout
import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequest.MergeRequestInfo
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequestIntegration.util.DateTimeUtil
import net.ntworld.mergeRequestIntegrationIde.util.HtmlHelper
import net.ntworld.mergeRequestIntegrationIde.component.Icons
import java.awt.Color
import java.awt.Cursor
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import javax.swing.BorderFactory
import javax.swing.JComponent
import javax.swing.JPanel

class CommentComponentImpl(
    private val groupComponent: GroupComponent,
    private val providerData: ProviderData,
    private val mergeRequestInfo: MergeRequestInfo,
    private val comment: Comment,
    private val indent: Int,
    private val borderLeftRight: Int = 1
) : CommentComponent {
    private val myPanel = SimpleToolWindowPanel(true, false)
    private val myNameLabel = Label(comment.author.name)
    private val myUsernameLabel = Label("@${comment.author.username}")
    private val myNameSeparatorLabel = Label("·")
    private var myUsePrettyTime: Boolean = true

    private val myWebView = TipUIUtil.createBrowser() as TipUIUtil.Browser
    private val myHtmlTemplate = CommentComponentImpl::class.java.getResource(
        "/templates/mr.comment.html"
    ).readText()
    private val myTimeAction = object : AnAction(null, null, null) {
        override fun actionPerformed(e: AnActionEvent) {
            myUsePrettyTime = !myUsePrettyTime
        }

        override fun update(e: AnActionEvent) {
            e.presentation.text = if (myUsePrettyTime) {
                DateTimeUtil.toPretty(DateTimeUtil.toDate(comment.updatedAt))
            } else {
                DateTimeUtil.formatDate(DateTimeUtil.toDate(comment.updatedAt))
            }
        }

        override fun useSmallerFontForTextInToolbar(): Boolean = false
        override fun displayTextInToolbar() = true
    }
    private val myOpenInBrowserAction = object : AnAction(
        "View in browser", "Open and view the comment in browser", Icons.ExternalLink
    ) {
        override fun actionPerformed(e: AnActionEvent) {
            BrowserUtil.open(providerData.info.createCommentUrl(mergeRequestInfo.url, comment))
        }
    }
    private val myReplyAction = object : AnAction(
        "Reply", "Reply this comment", Icons.ReplyComment
    ) {
        override fun actionPerformed(e: AnActionEvent) {
            groupComponent.showReplyEditor()
        }
    }
    private val myDeleteAction = object : AnAction(
        "Delete comment", "Delete comment", Icons.Trash
    ) {
        override fun actionPerformed(e: AnActionEvent) {
            val result = Messages.showYesNoDialog(
                "Do you want to delete the comment?", "Are you sure", Messages.getQuestionIcon()
            )
            if (result == Messages.YES) {
                groupComponent.requestDeleteComment(comment)
            }
        }
    }
    private val myResolveAction = object : AnAction() {
        override fun actionPerformed(e: AnActionEvent) {
            groupComponent.requestToggleResolvedStateOfComment(comment)
        }

        override fun update(e: AnActionEvent) {
            super.update(e)
            if (comment.resolved) {
                e.presentation.icon = Icons.Resolved
                e.presentation.description = "Unresolve thread"
                val resolvedBy = comment.resolvedBy
                if (null !== resolvedBy) {
                    e.presentation.text = "Resolved by ${resolvedBy.name}"
                }
            } else {
                e.presentation.icon = Icons.Resolve
                e.presentation.text = "Resolve thead"
                e.presentation.description = "Mark thread as resolved"
            }
        }
    }
    private val myNameMouseListener = object : MouseListener {
        override fun mouseReleased(e: MouseEvent?) {}
        override fun mouseEntered(e: MouseEvent?) {}
        override fun mousePressed(e: MouseEvent?) {}
        override fun mouseExited(e: MouseEvent?) {}

        override fun mouseClicked(e: MouseEvent?) {
            groupComponent.collapse = !groupComponent.collapse
            myNameLabel.icon = if (groupComponent.collapse) Icons.CaretRight else Icons.CaretDown
        }
    }

    init {
        if (indent == 0 && groupComponent.comments.size > 1) {
            myNameLabel.icon = if (groupComponent.collapse) Icons.CaretRight else Icons.CaretDown
            myNameLabel.addMouseListener(myNameMouseListener)
            myNameLabel.cursor = Cursor.getDefaultCursor()
        }

        myUsernameLabel.foreground = Color(153, 153, 153)
        myWebView.text = buildHtml(providerData, comment)
        myPanel.toolbar = createToolbar()
        myPanel.setContent(myWebView.component)

        myPanel.border = BorderFactory.createMatteBorder(
            0, indent * 40 + borderLeftRight, 1, borderLeftRight, JBColor.border()
        )
    }

    override val component: JComponent = myPanel

    private fun createToolbar(): JComponent {
        val panel = JPanel(MigLayout("ins 0, fill", "5[left]5[left]5[left]0[left, fill]push[right]", "center"))

        val leftActionGroupTwo = DefaultActionGroup()
        leftActionGroupTwo.add(myTimeAction)
        val leftToolbarTwo = ActionManager.getInstance().createActionToolbar(
            "${CommentComponentImpl::class.java.canonicalName}/toolbar-left-two",
            leftActionGroupTwo,
            true
        )

        val rightActionGroup = DefaultActionGroup()
        if (providerData.currentUser.id == comment.author.id) {
            rightActionGroup.add(myDeleteAction)
            rightActionGroup.addSeparator()
        }
        rightActionGroup.add(myOpenInBrowserAction)
        rightActionGroup.addSeparator()
        if (comment.resolvable) {
            rightActionGroup.add(myResolveAction)
        }
        rightActionGroup.add(myReplyAction)
        val rightToolbar = ActionManager.getInstance().createActionToolbar(
            "${CommentComponentImpl::class.java.canonicalName}/toolbar-right",
            rightActionGroup,
            true
        )

        panel.add(myNameLabel)
        panel.add(myUsernameLabel)
        panel.add(myNameSeparatorLabel)
        panel.add(leftToolbarTwo.component)
        panel.add(rightToolbar.component)
        return panel
    }

    private fun buildHtml(providerData: ProviderData, comment: Comment): String {
        val output = myHtmlTemplate
            .replace("{{content}}", HtmlHelper.convertFromMarkdown(comment.body))

        return HtmlHelper.resolveRelativePath(providerData, output)
    }
}