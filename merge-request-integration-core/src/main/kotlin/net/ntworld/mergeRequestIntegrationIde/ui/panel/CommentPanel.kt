package net.ntworld.mergeRequestIntegrationIde.ui.panel

import com.intellij.ide.BrowserUtil
import com.intellij.ide.util.TipUIUtil
import com.intellij.openapi.ui.Messages
import com.intellij.ui.JBColor
import com.intellij.ui.ScrollPaneFactory
import com.intellij.util.EventDispatcher
import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequest.MergeRequest
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequest.command.DeleteCommentCommand
import net.ntworld.mergeRequest.command.ResolveCommentCommand
import net.ntworld.mergeRequest.command.UnresolveCommentCommand
import net.ntworld.mergeRequestIntegration.make
import net.ntworld.mergeRequestIntegration.util.DateTimeUtil
import net.ntworld.mergeRequestIntegrationIde.service.ApplicationService
import net.ntworld.mergeRequestIntegrationIde.ui.Component
import net.ntworld.mergeRequestIntegrationIde.ui.mergeRequest.tab.MergeRequestDescriptionTab
import net.ntworld.mergeRequestIntegrationIde.ui.util.HtmlHelper
import net.ntworld.mergeRequestIntegrationIde.ui.util.Icons
import java.awt.Color
import java.awt.ComponentOrientation
import java.awt.Dimension
import java.awt.event.ActionListener
import java.util.*
import javax.swing.*

class CommentPanel : Component {
    var myWholePanel: JPanel? = null
    var myFullName: JLabel? = null
    var myUsername: JLabel? = null
    var myHeaderWrapper: JPanel? = null
    var myButtonWrapper: JPanel? = null
    var myContentWrapper: JPanel? = null
    var myTime: JLabel? = null
    val dispatcher = EventDispatcher.create(Listener::class.java)

    private val myReplyButton: JButton = JButton()
    private val myOpenButton: JButton = JButton()
    private val myDeleteButton: JButton = JButton()
    private val myResolveButton: JButton = JButton()
    private var myProviderData: ProviderData? = null
    private var myMergeRequest: MergeRequest? = null
    private var myComment: Comment? = null
    private var myUrl: String = ""
    private val myHtmlTemplate = MergeRequestDescriptionTab::class.java.getResource(
        "/templates/mr.comment.html"
    ).readText()

    private val myWebView = TipUIUtil.createBrowser() as TipUIUtil.Browser
    private val myResolveButtonActionListener = ActionListener {
        val providerData = myProviderData
        val mergeRequest = myMergeRequest
        val comment = myComment
        if (null === providerData || null === mergeRequest || null === comment) {
            return@ActionListener
        }

        val command = if (comment.resolved) {
            UnresolveCommentCommand.make(
                providerId = providerData.id,
                mergeRequestId = mergeRequest.id,
                comment = comment
            )
        } else {
            ResolveCommentCommand.make(
                providerId = providerData.id,
                mergeRequestId = mergeRequest.id,
                comment = comment
            )
        }
        ApplicationService.instance.infrastructure.commandBus() process command
        dispatcher.multicaster.onDestroyRequested(providerData, mergeRequest, comment)
    }
    private val myDeleteButtonActionListener = ActionListener {
        val providerData = myProviderData
        val mergeRequest = myMergeRequest
        val comment = myComment
        if (null === providerData || null === mergeRequest || null === comment) {
            return@ActionListener
        }

        val result = Messages.showYesNoDialog(
            "Do you want to delete the comment?", "Are you sure", Messages.getQuestionIcon()
        )
        if (result == Messages.YES) {
            ApplicationService.instance.infrastructure.commandBus() process DeleteCommentCommand.make(
                providerId = providerData.id,
                mergeRequestId = mergeRequest.id,
                comment = comment
            )
            dispatcher.multicaster.onDestroyRequested(providerData, mergeRequest, comment)
        }
    }

    init {
        myButtonWrapper!!.layout = BoxLayout(myButtonWrapper!!, BoxLayout.LINE_AXIS)
        myButtonWrapper!!.componentOrientation = ComponentOrientation.RIGHT_TO_LEFT

        myReplyButton.text = "Reply"

        myOpenButton.text = "View in browser"

        myDeleteButton.icon = Icons.Trash
        myDeleteButton.preferredSize = Dimension(27, 27)
        myDeleteButton.maximumSize = Dimension(27, 27)

        myResolveButton.preferredSize = Dimension(27, 27)
        myResolveButton.maximumSize = Dimension(27, 27)

        myButtonWrapper!!.add(myReplyButton)
        myButtonWrapper!!.add(myOpenButton)
        myButtonWrapper!!.add(myResolveButton)
        myButtonWrapper!!.add(myDeleteButton)

        myContentWrapper!!.add(ScrollPaneFactory.createScrollPane(myWebView.component))
        myContentWrapper!!.border = BorderFactory.createMatteBorder(1, 0, 0, 0, JBColor.border())
        myOpenButton.addActionListener {
            if (myUrl.isNotEmpty()) {
                BrowserUtil.open(myUrl)
            }
        }

        myDeleteButton.isVisible = false
        myResolveButton.isVisible = false
        myResolveButton.addActionListener(myResolveButtonActionListener)
        myDeleteButton.addActionListener(myDeleteButtonActionListener)
        myReplyButton.addActionListener {
            dispatcher.multicaster.onReplyButtonClick()
        }
    }

    fun setComment(providerData: ProviderData, mergeRequest: MergeRequest, comment: Comment) {
        myProviderData = providerData
        myMergeRequest = mergeRequest
        myComment = comment

        myFullName!!.text = comment.author.name
        myUsername!!.text = "@${comment.author.username}"
        myUrl = providerData.info.createCommentUrl(mergeRequest.url, comment)

        val createdAt = DateTimeUtil.toDate(comment.createdAt)
        myTime!!.text = "${DateTimeUtil.formatDate(createdAt)} · ${DateTimeUtil.toPretty(createdAt)}"

        myDeleteButton.isVisible = comment.author.id == providerData.currentUser.id
        myWebView.text = buildHtml(providerData, comment)

        if (comment.resolvable) {
            myResolveButton.isVisible = true
            myResolveButton.icon = if (comment.resolved) Icons.Resolved else Icons.Resolve
        }
    }

    private fun buildHtml(providerData: ProviderData, comment: Comment): String {
        val output = myHtmlTemplate
            .replace("{{content}}", HtmlHelper.convertFromMarkdown(comment.body))

        return HtmlHelper.resolveRelativePath(providerData, output)
    }

    override fun createComponent(): JComponent = myWholePanel!!

    interface Listener : EventListener {
        fun onReplyButtonClick()

        fun onDestroyRequested(
            providerData: ProviderData,
            mergeRequest: MergeRequest,
            comment: Comment
        )
    }
}