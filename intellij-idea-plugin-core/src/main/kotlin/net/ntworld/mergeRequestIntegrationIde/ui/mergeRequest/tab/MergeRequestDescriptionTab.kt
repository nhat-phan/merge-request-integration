package net.ntworld.mergeRequestIntegrationIde.ui.mergeRequest.tab

import com.intellij.ide.util.TipUIUtil
import net.ntworld.mergeRequest.MergeRequestInfo
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequestIntegrationIde.ui.util.HtmlHelper
import javax.swing.JComponent

class MergeRequestDescriptionTab : MergeRequestDescriptionTabUI {
    private var myMR: MergeRequestInfo? = null
    private val myWebView = TipUIUtil.createBrowser() as TipUIUtil.Browser
    private val myHtmlTemplate = MergeRequestDescriptionTab::class.java.getResource(
        "/templates/mr.description.html"
    ).readText()

    override fun setMergeRequestInfo(providerData: ProviderData, mr: MergeRequestInfo) {
        val currentMR = myMR
        if (null === currentMR || currentMR.id != mr.id) {
            myMR = mr
            myWebView.text = buildHtml(providerData, mr)
        }
    }

    private fun buildHtml(providerData: ProviderData, mr: MergeRequestInfo): String {
        val output = myHtmlTemplate
            .replace("{{title}}", mr.title)
            .replace("{{description}}", HtmlHelper.convertFromMarkdown(mr.description))

        return HtmlHelper.resolveRelativePath(providerData, output)
    }

    override fun createComponent(): JComponent {
        return myWebView.component
    }
}