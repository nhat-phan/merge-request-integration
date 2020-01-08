package net.ntworld.mergeRequestIntegrationIde.ui.toolWindowTab

import com.intellij.ide.util.TipUIUtil
import com.intellij.ui.ScrollPaneFactory
import net.ntworld.mergeRequestIntegrationIde.ui.Component
import net.ntworld.mergeRequestIntegrationIde.ui.mergeRequest.tab.MergeRequestDescriptionTab
import javax.swing.JComponent

class UpdateInfoTab(private val updates: List<String>) : Component {
    private val myWebView = TipUIUtil.createBrowser() as TipUIUtil.Browser
    private val myHtmlTemplate = MergeRequestDescriptionTab::class.java.getResource(
        "/templates/update.html"
    ).readText()

    init {
        myWebView.text = buildHtml()
    }

    private fun buildHtml() : String {
        return myHtmlTemplate
            .replace("{{content}}", updates.joinToString("<br /><br />"))
    }

    override fun createComponent(): JComponent = ScrollPaneFactory.createScrollPane(myWebView.component)
}