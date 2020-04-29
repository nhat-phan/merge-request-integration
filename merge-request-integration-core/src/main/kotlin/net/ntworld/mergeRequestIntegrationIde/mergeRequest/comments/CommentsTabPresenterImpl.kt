package net.ntworld.mergeRequestIntegrationIde.mergeRequest.comments

import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.util.EventDispatcher
import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequest.CommentPosition
import net.ntworld.mergeRequest.MergeRequestInfo
import net.ntworld.mergeRequest.command.DeleteCommentCommand
import net.ntworld.mergeRequest.command.ResolveCommentCommand
import net.ntworld.mergeRequest.command.UnresolveCommentCommand
import net.ntworld.mergeRequest.request.CreateCommentRequest
import net.ntworld.mergeRequest.request.ReplyCommentRequest
import net.ntworld.mergeRequestIntegration.make
import net.ntworld.mergeRequestIntegration.provider.ProviderException
import net.ntworld.mergeRequestIntegrationIde.AbstractPresenter
import net.ntworld.mergeRequestIntegrationIde.DataChangedSource
import net.ntworld.mergeRequestIntegrationIde.diff.DiffNotifier
import net.ntworld.mergeRequestIntegrationIde.infrastructure.ProjectServiceProvider
import net.ntworld.mergeRequestIntegrationIde.infrastructure.api.MergeRequestDataNotifier
import net.ntworld.mergeRequestIntegrationIde.mergeRequest.comments.tree.node.*
import net.ntworld.mergeRequestIntegrationIde.mergeRequest.isEmpty
import java.util.*

class CommentsTabPresenterImpl(
    private val projectServiceProvider: ProjectServiceProvider,
    override val model: CommentsTabModel,
    override val view: CommentsTabView
) : AbstractPresenter<EventListener>(),
    CommentsTabPresenter, CommentsTabModel.DataListener, CommentsTabView.ActionListener {
    override val dispatcher = EventDispatcher.create(EventListener::class.java)
    private val myDiffPublisher = projectServiceProvider.messageBus.syncPublisher(DiffNotifier.TOPIC)

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
                handleWhenCommentsGetUpdated()
            }
        } else {
            handleWhenCommentsGetUpdated()
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
            val reviewContext = projectServiceProvider.reviewContextManager.findContext(model.providerData.id, it.id)
            if (null !== reviewContext) {
                val change = reviewContext.findChangeByPath(node.path)
                if (null !== change) {
                    reviewContext.openChange(change, focus = false, displayMergeRequestId = !projectServiceProvider.isDoingCodeReview())
                    view.hideThread()
                }
            }
            return@assertMergeRequestInfoIsAvailable
        }

        if (node is FileLineNode) {
            displayGeneralComments(it, node)
            val reviewContext = projectServiceProvider.reviewContextManager.findContext(model.providerData.id, it.id)
            if (null !== reviewContext) {
                val change = reviewContext.findChangeByPath(node.path)
                if (null !== change) {
                    reviewContext.putChangeData(change, DiffNotifier.ScrollPosition, node.position)
                    reviewContext.putChangeData(change, DiffNotifier.ScrollShowComments, true)
                    reviewContext.openChange(change, focus = false, displayMergeRequestId = !projectServiceProvider.isDoingCodeReview())
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
            if (view.hasGeneralCommentsTreeNode()) {
                view.selectGeneralCommentsTreeNode()
            } else {
                view.renderThread(it, mapOf())
            }
            view.focusToMainEditor()
        }
    }

    override fun onRefreshButtonClicked() = requestFetchComments()

    override fun onDeleteCommentRequested(comment: Comment) = assertMergeRequestInfoIsAvailable {
        projectServiceProvider.infrastructure.commandBus() process DeleteCommentCommand.make(
            providerId = model.providerData.id,
            mergeRequestId = it.id,
            comment = comment
        )
        requestFetchComments()
    }

    override fun onResolveCommentRequested(comment: Comment) = assertMergeRequestInfoIsAvailable {
        projectServiceProvider.infrastructure.commandBus() process ResolveCommentCommand.make(
            providerId = model.providerData.id,
            mergeRequestId = it.id,
            comment = comment
        )
        requestFetchComments()
    }

    override fun onUnresolveCommentRequested(comment: Comment) = assertMergeRequestInfoIsAvailable {
        projectServiceProvider.infrastructure.commandBus() process UnresolveCommentCommand.make(
            providerId = model.providerData.id,
            mergeRequestId = it.id,
            comment = comment
        )
        requestFetchComments()
    }

    override fun onReplyCommentRequested(repliedComment: Comment, content: String) = assertMergeRequestInfoIsAvailable {
        projectServiceProvider.infrastructure.serviceBus() process ReplyCommentRequest.make(
            providerId = model.providerData.id,
            mergeRequestId = it.id,
            repliedComment = repliedComment,
            body = content
        ) ifError { exception ->
            projectServiceProvider.notify(
                "There was an error from server. \n\n ${exception.message}",
                NotificationType.ERROR
            )
            throw ProviderException(exception)
        }
        view.clearMainEditorText()
        requestFetchComments()
    }

    override fun onCreateCommentRequested(content: String, position: CommentPosition?) = assertMergeRequestInfoIsAvailable {
        projectServiceProvider.infrastructure.serviceBus() process CreateCommentRequest.make(
            providerId = model.providerData.id,
            mergeRequestId = model.mergeRequestInfo.id,
            position = position,
            body = content
        ) ifError {
            projectServiceProvider.notify(
                "There was an error from server. \n\n ${it.message}",
                NotificationType.ERROR
            )
            throw ProviderException(it)
        }
        view.clearMainEditorText()
        requestFetchComments()
    }

    private fun requestFetchComments() = assertMergeRequestInfoIsAvailable {
        projectServiceProvider.messageBus.syncPublisher(MergeRequestDataNotifier.TOPIC).fetchCommentsRequested(
            model.providerData, it
        )
    }

    private fun handleWhenCommentsGetUpdated() {
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