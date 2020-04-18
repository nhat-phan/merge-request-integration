package net.ntworld.mergeRequestIntegrationIde.mergeRequest.comments

import com.intellij.diff.util.Side
import com.intellij.openapi.application.ApplicationManager
import com.intellij.util.EventDispatcher
import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequest.CommentPosition
import net.ntworld.mergeRequest.MergeRequestInfo
import net.ntworld.mergeRequestIntegrationIde.AbstractPresenter
import net.ntworld.mergeRequestIntegrationIde.DataChangedSource
import net.ntworld.mergeRequestIntegrationIde.diff.DiffNotifier
import net.ntworld.mergeRequestIntegrationIde.infrastructure.ReviewContextManager
import net.ntworld.mergeRequestIntegrationIde.infrastructure.api.MergeRequestDataNotifier
import net.ntworld.mergeRequestIntegrationIde.mergeRequest.comments.tree.node.*
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
    private val myDiffPublisher = projectService.messageBus.syncPublisher(DiffNotifier.TOPIC)

    init {
        model.addDataListener(this)
        view.addActionListener(this)
    }

    override fun onMergeRequestInfoChanged() {
        if (model.mergeRequestInfo.isEmpty()) {
            view.hideThread()
        } else {
            requestFetchComments()
        }
    }

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

    /**
     * Node tree structure:
     *
     * + Root (hidden)
     *   - GeneralCommentNode
     *     - ThreadNode
     *       - CommentNode
     *   - FileNode
     *     - FileLineNode
     *       - ThreadNode
     *         - CommentNode
     */
    override fun onTreeNodeSelected(node: Node) = assertMergeRequestInfoIsAvailable {
        if (node is GeneralCommentsNode) {
            displayGeneralComments(it, node)
            return@assertMergeRequestInfoIsAvailable
        }

        if (node is ThreadNode) {
            displayGeneralComments(it, node.parent!!)
            return@assertMergeRequestInfoIsAvailable
        }

        if (node is CommentNode) {
            displayGeneralComments(it, node.parent!!.parent!!)
            return@assertMergeRequestInfoIsAvailable
        }

        if (node is FileNode) {
            val reviewContext = ReviewContextManager.findContext(model.providerData.id, it.id)
            if (null !== reviewContext) {
                val change = reviewContext.findChangeByPath(node.path)
                if (null !== change) {
                    reviewContext.openChange(change, focus = false, displayMergeRequestId = !projectService.isDoingCodeReview())
                    view.hideThread()
                }
            }
            return@assertMergeRequestInfoIsAvailable
        }

        if (node is FileLineNode) {
            displayGeneralComments(it, node)
            val reviewContext = ReviewContextManager.findContext(model.providerData.id, it.id)
            if (null !== reviewContext) {
                val change = reviewContext.findChangeByPath(node.path)
                if (null !== change) {
                    reviewContext.putChangeData(change, DiffNotifier.ScrollPosition, node.position)
                    reviewContext.putChangeData(change, DiffNotifier.ScrollShowComments, true)
                    reviewContext.openChange(change, focus = false, displayMergeRequestId = !projectService.isDoingCodeReview())
                    myDiffPublisher.hideAllCommentsRequested(reviewContext, change)
                    myDiffPublisher.scrollToPositionRequested(reviewContext, change, node.position, true)
                }
            }
        }
    }


    override fun onShowResolvedCommentsToggled(displayResolvedComments: Boolean) {
        model.displayResolvedComments = displayResolvedComments
    }

    override fun onCreateGeneralCommentClicked() {
        assertMergeRequestInfoIsAvailable {
            view.selectGeneralCommentsTreeNode()
            view.focusToMainEditor()
        }
    }

    override fun onRefreshButtonClicked() = requestFetchComments()

    private fun requestFetchComments() = assertMergeRequestInfoIsAvailable {
        projectService.messageBus.syncPublisher(MergeRequestDataNotifier.TOPIC).fetchCommentsRequested(
            model.providerData, it
        )
    }

    private fun handleWhenCommentsGetUpdated(source: DataChangedSource) {
        view.displayCommentCount(model.comments.size)
        view.hideThread()
        view.renderTree(model.mergeRequestInfo, model.comments, model.displayResolvedComments)
    }

    private fun assertMergeRequestInfoIsAvailable(invoker: ((MergeRequestInfo) -> Unit)) {
        val mergeRequestInfo = model.mergeRequestInfo
        if (!mergeRequestInfo.isEmpty()) {
            invoker.invoke(mergeRequestInfo)
        }
    }

    private fun displayGeneralComments(mergeRequestInfo: MergeRequestInfo, parent: Node) {
        val groupedComments = mutableMapOf<String, MutableList<Comment>>()
        parent.children.forEach {
            if (it !is ThreadNode) {
                return@forEach
            }
            groupedComments[it.threadId] = mutableListOf(it.comment)
            it.children.forEach { node ->
                if (node is CommentNode) {
                    groupedComments[it.threadId]!!.add(node.comment)
                }
            }
        }

        view.renderThread(mergeRequestInfo, groupedComments)
    }
}