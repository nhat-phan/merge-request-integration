package net.ntworld.mergeRequestIntegrationIde.toolWindow.internal

import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequest.MergeRequestInfo
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequestIntegrationIde.infrastructure.ProjectServiceProvider
import net.ntworld.mergeRequestIntegrationIde.infrastructure.notifier.ReworkWatcherNotifier
import net.ntworld.mergeRequestIntegrationIde.mergeRequest.comments.tree.CommentTreeFactory
import net.ntworld.mergeRequestIntegrationIde.mergeRequest.comments.tree.CommentTreePresenter
import net.ntworld.mergeRequestIntegrationIde.mergeRequest.comments.tree.CommentTreeView
import net.ntworld.mergeRequestIntegrationIde.mergeRequest.comments.tree.node.Node
import net.ntworld.mergeRequestIntegrationIde.toolWindow.CommentsToolWindowTab
import javax.swing.JComponent

class CommentsToolWindowTabImpl(
    private val projectServiceProvider: ProjectServiceProvider,
    override val providerData: ProviderData,
    mergeRequestInfo: MergeRequestInfo,
    comments: List<Comment>
): CommentsToolWindowTab {
    private val myPublisher = projectServiceProvider.messageBus.syncPublisher(
        ReworkWatcherNotifier.TOPIC
    )
    private val myTreePresenter: CommentTreePresenter = CommentTreeFactory.makePresenter(
        CommentTreeFactory.makeModel(providerData),
        CommentTreeFactory.makeView(projectServiceProvider, providerData, showOpenDiffViewDescription = true)
    )
    private val myTreePresenterListener = object: CommentTreePresenter.Listener {
        override fun onTreeNodeSelected(node: Node, type: CommentTreeView.TreeSelectType) {
            myPublisher.commentTreeNodeSelected(providerData, node, type)
        }

        override fun onShowResolvedCommentsToggled(displayResolvedComments: Boolean) {
            myPublisher.changeDisplayResolvedComments(providerData, displayResolvedComments)
        }

        override fun onCreateGeneralCommentClicked() {
            myPublisher.openCreateGeneralCommentForm(providerData)
        }

        override fun onRefreshButtonClicked() {
            myPublisher.requestFetchComment(providerData)
        }
    }

    init {
        myTreePresenter.setToolbarMode(CommentTreeView.ToolbarMode.MINI)
        myTreePresenter.model.displayResolvedComments = false
        myTreePresenter.model.comments = comments
        myTreePresenter.model.mergeRequestInfo = mergeRequestInfo
        myTreePresenter.addListener(myTreePresenterListener)
    }

    override val component: JComponent = myTreePresenter.component

    override fun setMergeRequestInfo(mergeRequestInfo: MergeRequestInfo) {
        myTreePresenter.model.mergeRequestInfo = mergeRequestInfo
    }

    override fun setComments(comments: List<Comment>) {
        myTreePresenter.model.comments = comments
    }

    override fun setDisplayResolvedComments(value: Boolean) {
        myTreePresenter.model.displayResolvedComments = value
    }
}