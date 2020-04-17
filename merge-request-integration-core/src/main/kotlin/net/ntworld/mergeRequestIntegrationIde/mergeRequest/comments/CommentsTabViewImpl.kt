package net.ntworld.mergeRequestIntegrationIde.mergeRequest.comments

import com.intellij.ui.OnePixelSplitter
import com.intellij.ui.tabs.TabInfo
import com.intellij.util.EventDispatcher
import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequest.MergeRequestInfo
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequestIntegrationIde.AbstractView
import net.ntworld.mergeRequestIntegrationIde.mergeRequest.comments.tree.CommentTreeFactory
import net.ntworld.mergeRequestIntegrationIde.mergeRequest.comments.tree.CommentTreePresenter
import net.ntworld.mergeRequestIntegrationIde.service.ProjectService
import net.ntworld.mergeRequestIntegrationIde.component.Icons
import net.ntworld.mergeRequestIntegrationIde.mergeRequest.comments.tree.node.Node
import javax.swing.JComponent
import javax.swing.JPanel

class CommentsTabViewImpl(
    private val projectService: ProjectService,
    private val providerData: ProviderData
) : AbstractView<CommentsTabView.ActionListener>(), CommentsTabView {
    override val dispatcher = EventDispatcher.create(CommentsTabView.ActionListener::class.java)

    private val mySplitter = OnePixelSplitter(
        this::class.java.canonicalName,
        0.5f
    )
    private val myTreePresenter: CommentTreePresenter = CommentTreeFactory.makePresenter(
        CommentTreeFactory.makeModel(providerData),
        CommentTreeFactory.makeView(projectService, providerData)
    )
    private val myTreeListener = object : CommentTreePresenter.Listener {
        override fun onTreeNodeSelected(node: Node) {
            dispatcher.multicaster.onTreeNodeSelected(node)
        }

        override fun onShowResolvedCommentsToggled(displayResolvedComments: Boolean) {
            dispatcher.multicaster.onShowResolvedCommentsToggled(displayResolvedComments)
        }

        override fun onCreateGeneralCommentClicked() {
            dispatcher.multicaster.onCreateGeneralCommentClicked()
        }

        override fun onRefreshButtonClicked() {
            dispatcher.multicaster.onRefreshButtonClicked()
        }
    }

    override val component: JComponent = mySplitter

    override val tabInfo: TabInfo by lazy {
        val tabInfo = TabInfo(component)
        tabInfo.text = "Comments"
        tabInfo.icon = Icons.Comments
        tabInfo
    }

    init {
        mySplitter.firstComponent = myTreePresenter.component
        mySplitter.secondComponent = JPanel()

        myTreePresenter.addListener(myTreeListener)
    }

    override fun displayCommentCount(count: Int) {
        tabInfo.text = if (0 == count) "Comments" else "Comments · $count"
    }

    override fun dispose() {
    }

    override fun renderTree(
        mergeRequestInfo: MergeRequestInfo, comments: List<Comment>, displayResolvedComments: Boolean
    ) {
        myTreePresenter.model.mergeRequestInfo = mergeRequestInfo
        myTreePresenter.model.comments = comments
        myTreePresenter.model.displayResolvedComments = displayResolvedComments
    }
}