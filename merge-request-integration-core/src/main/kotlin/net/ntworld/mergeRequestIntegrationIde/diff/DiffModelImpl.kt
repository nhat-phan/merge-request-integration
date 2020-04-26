package net.ntworld.mergeRequestIntegrationIde.diff

import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vcs.changes.Change
import com.intellij.openapi.vcs.changes.ContentRevision
import com.intellij.util.EventDispatcher
import com.intellij.util.messages.MessageBusConnection
import net.ntworld.mergeRequest.*
import net.ntworld.mergeRequestIntegrationIde.AbstractModel
import net.ntworld.mergeRequestIntegrationIde.DataChangedSource
import net.ntworld.mergeRequestIntegrationIde.infrastructure.ReviewContext
import net.ntworld.mergeRequestIntegrationIde.infrastructure.api.MergeRequestDataNotifier
import net.ntworld.mergeRequestIntegrationIde.infrastructure.ProjectServiceProvider
import net.ntworld.mergeRequestIntegrationIde.util.RepositoryUtil

class DiffModelImpl(
    private val projectServiceProvider: ProjectServiceProvider,
    override val reviewContext: ReviewContext,
    override val change: Change,
    override var displayResolvedComments: Boolean
) : AbstractModel<DiffModel.DataListener>(), DiffModel {
    override val dispatcher = EventDispatcher.create(DiffModel.DataListener::class.java)
    private val commentFilterOnBeforeSide: ((ContentRevision, Comment) -> Boolean) = { revision, comment ->
        val position = comment.position
        if (null === position || null === position.oldPath) {
            false
        } else {
            RepositoryUtil.findAbsoluteCrossPlatformsPath(reviewContext.repository, position.oldPath!!) ==
                RepositoryUtil.transformToCrossPlatformsPath(revision.file.path)
        }
    }
    private val commentFilterOnAfterSide: ((ContentRevision, Comment) -> Boolean) = { revision, comment ->
        val position = comment.position
        if (null === position || null === position.newPath) {
            false
        } else {
            RepositoryUtil.findAbsoluteCrossPlatformsPath(reviewContext.repository, position.newPath!!) ==
                RepositoryUtil.transformToCrossPlatformsPath(revision.file.path)
        }
    }
    private val commentFactoryOnBeforeSide: ((CommentPosition, Comment) -> CommentPoint) = { position, comment ->
        CommentPoint(position.oldLine!!, comment)
    }
    private val commentFactoryOnAfterSide: ((CommentPosition, Comment) -> CommentPoint) = { position, comment ->
        CommentPoint(position.newLine!!, comment)
    }

    override val messageBusConnection: MessageBusConnection = reviewContext.messageBusConnection
    private val myMergeRequestDataNotifier = object : MergeRequestDataNotifier {
        override fun fetchCommentsRequested(providerData: ProviderData, mergeRequestInfo: MergeRequestInfo) {
        }

        override fun onCommentsUpdated(
            providerData: ProviderData,
            mergeRequestInfo: MergeRequestInfo,
            comments: List<Comment>
        ) {
            if (providerData.id != reviewContext.providerData.id ||
                mergeRequestInfo.id != reviewContext.mergeRequestInfo.id) {
                return
            }
            buildCommentsOnBeforeSide(comments)
            buildCommentsOnAfterSide(comments)
            dispatcher.multicaster.onCommentsUpdated(DataChangedSource.NOTIFIER)
        }
    }

    override val providerData = reviewContext.providerData
    override val mergeRequestInfo = reviewContext.mergeRequestInfo
    override val diffReference: DiffReference?
        get() = reviewContext.diffReference
    override val commits
        get() = reviewContext.commits

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

    override fun rebuildComments(showResolved: Boolean) {
        displayResolvedComments = showResolved
        buildCommentsOnBeforeSide(null)
        buildCommentsOnAfterSide(null)
        dispatcher.multicaster.onCommentsUpdated(DataChangedSource.UI)
    }

    private fun buildCommentPoints(
        result: MutableList<CommentPoint>,
        revision: ContentRevision?,
        comments: List<Comment>?,
        filter: ((ContentRevision, Comment) -> Boolean),
        factory: ((CommentPosition, Comment) -> CommentPoint),
        matcher: ((CommentPosition, ContentRevision, List<String>) -> Boolean)
    ) {
        result.clear()
        if (null === revision) {
            return
        }
        val commitIds = reviewContext.commits.map { it.id }

        val list = if (null !== comments) {
            comments.filter {
                if (!displayResolvedComments && it.resolved) {
                    false
                } else {
                    filter.invoke(revision, it)
                }
            }
        } else {
            reviewContext.getCommentsByPath(revision.file.path).filter {
                displayResolvedComments || !it.resolved
            }
        }
        for (comment in list) {
            val position = comment.position
            if (null === position) {
                continue
            }
            if (matcher.invoke(position, revision, commitIds)) {
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
    ) { position, revision, commits ->
        val hash = revision.revisionNumber.asString()
        null !== position.oldLine && (position.startHash == hash || position.baseHash == hash || commits.contains(hash))
    }

    private fun buildCommentsOnAfterSide(comments: List<Comment>?) = buildCommentPoints(
        commentsOnAfterSide,
        change.afterRevision,
        comments,
        commentFilterOnAfterSide,
        commentFactoryOnAfterSide
    ) { position, revision, commits ->
        val hash = revision.revisionNumber.asString()
        null !== position.newLine && (position.headHash == hash || position.baseHash == hash || commits.contains(hash))
    }
}