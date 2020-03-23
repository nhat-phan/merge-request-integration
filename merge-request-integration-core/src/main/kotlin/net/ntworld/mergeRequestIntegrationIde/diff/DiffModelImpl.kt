package net.ntworld.mergeRequestIntegrationIde.diff

import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vcs.changes.Change
import com.intellij.openapi.vcs.changes.ContentRevision
import com.intellij.util.EventDispatcher
import com.intellij.util.messages.MessageBusConnection
import net.ntworld.mergeRequest.*
import net.ntworld.mergeRequestIntegrationIde.AbstractModel
import net.ntworld.mergeRequestIntegrationIde.infrastructure.api.MergeRequestDataNotifier
import net.ntworld.mergeRequestIntegrationIde.service.CodeReviewManager
import net.ntworld.mergeRequestIntegrationIde.ui.editor.CommentPoint
import net.ntworld.mergeRequestIntegrationIde.ui.util.RepositoryUtil

class DiffModelImpl(
    private val codeReviewManager: CodeReviewManager,
    override val change: Change
) : AbstractModel<DiffModel.DataListener>(), DiffModel {
    override val dispatcher = EventDispatcher.create(DiffModel.DataListener::class.java)
    private val commentFilterOnBeforeSide: ((ContentRevision, Comment) -> Boolean) = { revision, comment ->
        val position = comment.position
        if (null === position || null === position.oldPath) {
            false
        } else {
            RepositoryUtil.findAbsolutePath(codeReviewManager.repository, position.oldPath!!) == revision.file.path
        }
    }
    private val commentFilterOnAfterSide: ((ContentRevision, Comment) -> Boolean) = { revision, comment ->
        val position = comment.position
        if (null === position || null === position.newPath) {
            false
        } else {
            RepositoryUtil.findAbsolutePath(codeReviewManager.repository, position.newPath!!) == revision.file.path
        }
    }
    private val commentFactoryOnBeforeSide: ((CommentPosition, Comment) -> CommentPoint) = { position, comment ->
        CommentPoint(position.oldLine!!, comment)
    }
    private val commentFactoryOnAfterSide: ((CommentPosition, Comment) -> CommentPoint) = { position, comment ->
        CommentPoint(position.newLine!!, comment)
    }

    private val messageBusConnection: MessageBusConnection = codeReviewManager.messageBusConnection
    private val myMergeRequestDataNotifier = object : MergeRequestDataNotifier {
        override fun fetchCommentsRequested(providerData: ProviderData, mergeRequest: MergeRequest) {
        }

        override fun onCommentsUpdated(
            providerData: ProviderData,
            mergeRequest: MergeRequest,
            comments: List<Comment>
        ) {
            if (providerData.id != codeReviewManager.providerData.id ||
                mergeRequest.id != codeReviewManager.mergeRequest.id) {
                return
            }
            buildCommentsOnBeforeSide(comments)
            buildCommentsOnAfterSide(comments)
            dispatcher.multicaster.onCommentsUpdated()
        }
    }

    override val providerData = codeReviewManager.providerData
    override val mergeRequest = codeReviewManager.mergeRequest
    override val commits = codeReviewManager.commits.toList()

    override var commentsOnBeforeSide = mutableListOf<CommentPoint>()
        private set

    override var commentsOnAfterSide = mutableListOf<CommentPoint>()
        private set

    init {
        messageBusConnection.subscribe(MergeRequestDataNotifier.TOPIC, myMergeRequestDataNotifier)
        Disposer.register(messageBusConnection, this)
        buildCommentsOnBeforeSide(null)
        buildCommentsOnAfterSide(null)
    }

    override fun dispose() {
        dispatcher.listeners.clear()
        commentsOnBeforeSide.clear()
        commentsOnAfterSide.clear()
    }

    private fun buildCommentPoints(
        result: MutableList<CommentPoint>,
        revision: ContentRevision?,
        comments: List<Comment>?,
        filter: ((ContentRevision, Comment) -> Boolean),
        factory: ((CommentPosition, Comment) -> CommentPoint),
        matcher: ((CommentPosition, ContentRevision) -> Boolean)
    ) {
        result.clear()
        if (null === revision) {
            return
        }

        val list = if (null !== comments) {
            comments.filter {
                filter.invoke(revision, it)
            }
        } else {
            codeReviewManager.getCommentsByPath(revision.file.path)
        }
        for (comment in list) {
            val position = comment.position
            if (null === position) {
                continue
            }
            if (matcher.invoke(position, revision)) {
                result.add(factory(position, comment))
                continue
            }
        }
    }

    private fun buildCommentsOnBeforeSide(comments: List<Comment>?) = buildCommentPoints(
        commentsOnBeforeSide,
        change.beforeRevision,
        comments,
        commentFilterOnBeforeSide,
        commentFactoryOnBeforeSide
    ) { position, revision ->
        val hash = revision.revisionNumber.asString()
        null !== position.oldLine && (position.startHash == hash || position.baseHash == hash)
    }

    private fun buildCommentsOnAfterSide(comments: List<Comment>?) = buildCommentPoints(
        commentsOnAfterSide,
        change.afterRevision,
        comments,
        commentFilterOnAfterSide,
        commentFactoryOnAfterSide
    ) { position, revision ->
        val hash = revision.revisionNumber.asString()
        null !== position.newLine && (position.headHash == hash || position.baseHash == hash)
    }
}