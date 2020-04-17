package net.ntworld.mergeRequestIntegrationIde.mergeRequest.comments

import com.intellij.openapi.application.ApplicationManager
import com.intellij.util.EventDispatcher
import net.ntworld.mergeRequest.MergeRequestInfo
import net.ntworld.mergeRequestIntegrationIde.AbstractPresenter
import net.ntworld.mergeRequestIntegrationIde.DataChangedSource
import net.ntworld.mergeRequestIntegrationIde.infrastructure.ReviewContextManager
import net.ntworld.mergeRequestIntegrationIde.infrastructure.api.MergeRequestDataNotifier
import net.ntworld.mergeRequestIntegrationIde.mergeRequest.comments.tree.node.FileNode
import net.ntworld.mergeRequestIntegrationIde.mergeRequest.comments.tree.node.Node
import net.ntworld.mergeRequestIntegrationIde.mergeRequest.isEmpty
import net.ntworld.mergeRequestIntegrationIde.service.ApplicationService
import net.ntworld.mergeRequestIntegrationIde.service.ProjectService
import java.util.*

class CommentsTabPresenterImpl(
    private val applicationService: ApplicationService,
    private val projectService: ProjectService,
    override val model: CommentsTabModel,
    override val view: CommentsTabView
) : AbstractPresenter<EventListener>(),
    CommentsTabPresenter, CommentsTabModel.DataListener, CommentsTabView.ActionListener {
    override val dispatcher = EventDispatcher.create(EventListener::class.java)

    init {
        model.addDataListener(this)
        view.addActionListener(this)
    }

    override fun onMergeRequestInfoChanged() = requestFetchComments()

    override fun onCommentsUpdated(source: DataChangedSource) {
        if (source == DataChangedSource.NOTIFIER) {
            ApplicationManager.getApplication().invokeLater {
                handleWhenCommentsGetUpdated(source)
            }
        } else {
            handleWhenCommentsGetUpdated(source)
        }

    }

    override fun dispose() {
        view.dispose()
        model.dispose()
    }

    override fun onTreeNodeSelected(node: Node) = assertMergeRequestInfoIsAvailable {
        if (node is FileNode) {
            val reviewContext = ReviewContextManager.findContext(model.providerData.id, it.id)
            if (null !== reviewContext) {
                val change = reviewContext.findChangeByPath(node.path)
                if (null !== change) {
                    reviewContext.openChange(change)
                }
            }
        }
    }

    override fun onShowResolvedCommentsToggled(displayResolvedComments: Boolean) {
        model.displayResolvedComments = displayResolvedComments
    }

    override fun onCreateGeneralCommentClicked() {
    }

    override fun onRefreshButtonClicked() = requestFetchComments()

    private fun requestFetchComments() = assertMergeRequestInfoIsAvailable {
        projectService.messageBus.syncPublisher(MergeRequestDataNotifier.TOPIC).fetchCommentsRequested(
            model.providerData, it
        )
    }

    private fun handleWhenCommentsGetUpdated(source: DataChangedSource) {
        view.displayCommentCount(model.comments.size)
        view.renderTree(model.mergeRequestInfo, model.comments, model.displayResolvedComments)
    }

    private fun assertMergeRequestInfoIsAvailable(invoker: ((MergeRequestInfo) -> Unit)) {
        val mergeRequestInfo = model.mergeRequestInfo
        if (!mergeRequestInfo.isEmpty()) {
            invoker.invoke(mergeRequestInfo)
        }
    }
}