package net.ntworld.mergeRequestIntegrationIde.ui.util

import net.ntworld.mergeRequest.ProviderData
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer

object HtmlHelper {
    private val myCommonMarkParser = Parser.builder().build()
    private val myHtmlRenderer = HtmlRenderer.builder().build()

    fun convertFromMarkdown(md: String): String {
        return myHtmlRenderer.render(myCommonMarkParser.parse(md))
    }

    fun resolveRelativePath(providerData: ProviderData, html: String): String {
        return html.replace("<img src=\"/", "<img src=\"${providerData.project.url}/")
    }
}