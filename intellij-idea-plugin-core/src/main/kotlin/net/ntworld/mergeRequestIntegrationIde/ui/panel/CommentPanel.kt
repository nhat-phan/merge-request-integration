package net.ntworld.mergeRequestIntegrationIde.ui.panel

import com.intellij.ide.BrowserUtil
import com.intellij.ide.util.TipUIUtil
import com.intellij.ui.JBColor
import com.intellij.ui.ScrollPaneFactory
import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequest.MergeRequest
import net.ntworld.mergeRequest.MergeRequestInfo
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequestIntegration.util.DateTimeUtil
import net.ntworld.mergeRequestIntegrationIde.ui.Component
import net.ntworld.mergeRequestIntegrationIde.ui.mergeRequest.tab.MergeRequestDescriptionTab
import net.ntworld.mergeRequestIntegrationIde.ui.util.HtmlHelper
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer
import java.awt.event.ActionListener
import javax.swing.*

class CommentPanel : Component {
    var myWholePanel: JPanel? = null
    var myFullName: JLabel? = null
    var myUsername: JLabel? = null
    var myReplyButton: JButton? = null
    var myOpenButton: JButton? = null
    var myHeaderWrapper: JPanel? = null
    var myContentWrapper: JPanel? = null
    var myTime: JLabel? = null
    private var myUrl: String = ""
    private val myHtmlTemplate = MergeRequestDescriptionTab::class.java.getResource(
        "/templates/mr.comment.html"
    ).readText()

    private val myWebView = TipUIUtil.createBrowser() as TipUIUtil.Browser

    init {
        myContentWrapper!!.add(ScrollPaneFactory.createScrollPane(myWebView.component))
        myContentWrapper!!.border = BorderFactory.createMatteBorder(1, 0, 0, 0, JBColor.border())
        myOpenButton!!.addActionListener {
            if (myUrl.isNotEmpty()) {
                BrowserUtil.open(myUrl)
            }
        }
    }

    fun setComment(providerData: ProviderData, mergeRequest: MergeRequest, comment: Comment) {
        myFullName!!.text = comment.author.name
        myUsername!!.text = "@${comment.author.username}"
        myUrl = providerData.info.createCommentUrl(mergeRequest.url, comment)

        val createdAt = DateTimeUtil.toDate(comment.createdAt)
        myTime!!.text = "${DateTimeUtil.formatDate(createdAt)} · ${DateTimeUtil.toPretty(createdAt)}"

        myWebView.text = buildHtml(providerData, comment)
    }

    private fun buildHtml(providerData: ProviderData, comment: Comment): String {
        val output = myHtmlTemplate
            .replace("{{content}}", HtmlHelper.convertFromMarkdown(comment.body))

        return HtmlHelper.resolveRelativePath(providerData, output)
    }

    fun addReplyButtonActionListener(actionListener: ActionListener) {
        myReplyButton!!.addActionListener(actionListener)
    }

    override fun createComponent(): JComponent = myWholePanel!!
}