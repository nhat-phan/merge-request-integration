package net.ntworld.mergeRequestIntegrationIde.component.comment

import com.intellij.icons.AllIcons
import com.intellij.ide.BrowserUtil
import com.intellij.ide.util.TipUIUtil
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.ui.DialogBuilder
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.ui.JBColor
import com.intellij.ui.components.Label
import net.miginfocom.swing.MigLayout
import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequest.MergeRequestInfo
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequestIntegration.util.DateTimeUtil
import net.ntworld.mergeRequestIntegrationIde.ENTERPRISE_EDITION_URL
import net.ntworld.mergeRequestIntegrationIde.component.Icons
import net.ntworld.mergeRequestIntegrationIde.component.dialog.LegalWarningDialog
import net.ntworld.mergeRequestIntegrationIde.infrastructure.ProjectServiceProvider
import net.ntworld.mergeRequestIntegrationIde.util.HtmlHelper
import java.awt.Color
import java.awt.Cursor
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import javax.swing.BorderFactory
import javax.swing.JComponent
import javax.swing.JPanel

class CommentComponentImpl(
    private val projectServiceProvider: ProjectServiceProvider,
    private val groupComponent: GroupComponent,
    private val providerData: ProviderData,
    private val mergeRequestInfo: MergeRequestInfo,
    private val comment: Comment,
    private val indent: Int,
    private val options: Options
) : CommentComponent {
    private var displayMoveToDialog: Boolean = options.showMoveToDialog
    private val myPanel = SimpleToolWindowPanel(true, false)
    private val myNameLabel = Label(comment.author.name)
    private val myUsernameLabel = Label("@${comment.author.username}")
    private val myNameSeparatorLabel = Label("·")
    private var myUsePrettyTime: Boolean = true

    private val myWebView = TipUIUtil.createBrowser() as TipUIUtil.Browser
    private val myHtmlTemplate = CommentComponentImpl::class.java.getResource(
        "/templates/mr.comment.html"
    ).readText()

    private val myTimeAction = MyTimeAction(this)
    private val myDraftStatusAction = MyDraftStatusAction(this)
    private val myOpenInBrowserAction = MyOpenInBrowserAction(this)
    private val myReplyAction = MyReplyAction(this)
    private val myDeleteAction = MyDeleteAction(this)
    private val myMoveToDialogAction = MyMoveToDialogAction(this)
    private val myLegalWarningAction = MyLegalWarningAction(this)

    private class MyResolveAction(private val self: CommentComponentImpl) : AnAction() {
        override fun actionPerformed(e: AnActionEvent) {
            self.groupComponent.requestToggleResolvedStateOfComment(self.comment)
        }

        override fun update(e: AnActionEvent) {
            super.update(e)
            if (self.comment.resolved) {
                e.presentation.icon = Icons.Resolved
                e.presentation.description = "Unresolve thread"
                val resolvedBy = self.comment.resolvedBy
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
    private val myResolveAction = MyResolveAction(this)

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

    override val component: JComponent = myPanel

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
            0, indent * 40 + options.borderLeftRight, 1, options.borderLeftRight, JBColor.border()
        )
    }

    override fun hideMoveToDialogButtons() {
        displayMoveToDialog = false
    }

    override fun showMoveToDialogButtons() {
        displayMoveToDialog = true
    }

    private fun createToolbar(): JComponent {
        val panel = JPanel(MigLayout("ins 0, fill", "5[left]5[left]5[left]0[left, fill]push[right]", "center"))

        val leftActionGroupTwo = DefaultActionGroup()
        leftActionGroupTwo.add(myTimeAction)
        leftActionGroupTwo.add(myDraftStatusAction)
        val leftToolbarTwo = ActionManager.getInstance().createActionToolbar(
            "${CommentComponentImpl::class.java.canonicalName}/toolbar-left-two",
            leftActionGroupTwo,
            true
        )

        val rightActionGroup = DefaultActionGroup()
        if (options.showMoveToDialog) {
            rightActionGroup.add(myMoveToDialogAction)
            rightActionGroup.addSeparator()
        }

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

        if (!projectServiceProvider.applicationServiceProvider.isLegal(providerData)) {
            rightActionGroup.addSeparator()
            rightActionGroup.add(myLegalWarningAction)
        }

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

    private class MyMoveToDialogAction(private val self: CommentComponentImpl): AnAction(
        "Open in a Dialog", "", AllIcons.Actions.MoveToWindow
    ) {
        override fun actionPerformed(e: AnActionEvent) {
            self.groupComponent.requestOpenDialog()
        }

        override fun update(e: AnActionEvent) {
            super.update(e)
            e.presentation.isVisible = self.displayMoveToDialog
        }
    }

    private class MyTimeAction(private val self: CommentComponentImpl) : AnAction(null, null, null) {
        override fun actionPerformed(e: AnActionEvent) {
            self.myUsePrettyTime = !self.myUsePrettyTime
        }

        override fun update(e: AnActionEvent) {
            e.presentation.text = if (self.myUsePrettyTime) {
                DateTimeUtil.toPretty(DateTimeUtil.toDate(self.comment.updatedAt))
            } else {
                DateTimeUtil.formatDate(DateTimeUtil.toDate(self.comment.updatedAt))
            }
            e.presentation.isVisible = !self.comment.isDraft
        }

        override fun useSmallerFontForTextInToolbar(): Boolean = false
        override fun displayTextInToolbar() = true
    }

    private class MyDraftStatusAction(private val self: CommentComponentImpl) : AnAction(
        "Draft, click to publish this comment", "This is a draft comment, click to publish", null
    ) {
        override fun actionPerformed(e: AnActionEvent) {
            // TODO: publish a comment
        }

        override fun update(e: AnActionEvent) {
            e.presentation.isVisible = self.comment.isDraft
        }

        override fun useSmallerFontForTextInToolbar(): Boolean = false
        override fun displayTextInToolbar() = true
    }

    private class MyOpenInBrowserAction(private val self: CommentComponentImpl): AnAction(
        "View in browser", "Open and view the comment in browser", Icons.ExternalLink
    ) {
        override fun actionPerformed(e: AnActionEvent) {
            BrowserUtil.open(self.providerData.info.createCommentUrl(self.mergeRequestInfo.url, self.comment))
        }
    }

    private class MyReplyAction(private val self: CommentComponentImpl): AnAction(
        "Reply", "Reply this comment", Icons.ReplyComment
    ) {
        override fun actionPerformed(e: AnActionEvent) {
            self.groupComponent.showReplyEditor()
            if (self.options.replyInDialog) {
                self.groupComponent.requestOpenDialog()
            }
        }
    }

    private class MyDeleteAction(private val self: CommentComponentImpl): AnAction(
        "Delete comment", "Delete comment", Icons.Trash
    ) {
        override fun actionPerformed(e: AnActionEvent) {
            val result = Messages.showYesNoDialog(
                "Do you want to delete the comment?", "Are you sure", Messages.getQuestionIcon()
            )
            if (result == Messages.YES) {
                self.groupComponent.requestDeleteComment(self.comment)
            }
        }
    }

    private class MyLegalWarningAction(private val self: CommentComponentImpl): AnAction(
        "Illegal",
        "You cannot use CE for private repositories, please buy Enterprise Edition, only 1\$/month.",
        Icons.LegalWarning
    ) {
        override fun actionPerformed(e: AnActionEvent) {
            val builder = DialogBuilder()
            builder.title("Please buy Enterprise Edition")
            builder.setCenterPanel(LegalWarningDialog().component)
            builder.addOkAction()
            builder.okAction.setText("Buy Enterprise Edition")

            val code = builder.show()

            // This line run after the dialog is closed
            if (code == DialogWrapper.OK_EXIT_CODE) {
                BrowserUtil.open(ENTERPRISE_EDITION_URL)
            }
        }
    }
}