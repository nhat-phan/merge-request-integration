package net.ntworld.mergeRequestIntegrationIde.mergeRequest.comments

import com.intellij.ui.tabs.TabInfo
import com.intellij.util.EventDispatcher
import net.ntworld.mergeRequestIntegrationIde.AbstractView
import net.ntworld.mergeRequestIntegrationIde.ui.util.Icons
import javax.swing.JComponent
import javax.swing.JPanel

class CommentsTabViewImpl() : AbstractView<CommentsTabView.ActionListener>(), CommentsTabView {
    override val dispatcher = EventDispatcher.create(CommentsTabView.ActionListener::class.java)

    private val myPanel = JPanel()

    override val component: JComponent = myPanel

    override val tabInfo: TabInfo by lazy {
        val tabInfo = TabInfo(component)
        tabInfo.icon = Icons.Comments
        tabInfo
    }

    override fun displayCommentCount(count: Int) {
        tabInfo.text = if (0 == count) "Comments" else "Comments · $count"
    }

    override fun dispose() {
    }

    override fun renderTree() {
    }
}