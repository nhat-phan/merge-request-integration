package net.ntworld.mergeRequestIntegrationIde.diff

import com.intellij.notification.NotificationType
import com.intellij.openapi.util.Disposer
import com.intellij.util.EventDispatcher
import git4idea.repo.GitRepository
import net.ntworld.mergeRequest.*
import net.ntworld.mergeRequest.command.DeleteCommentCommand
import net.ntworld.mergeRequest.command.ResolveCommentCommand
import net.ntworld.mergeRequest.command.UnresolveCommentCommand
import net.ntworld.mergeRequest.request.CreateCommentRequest
import net.ntworld.mergeRequest.request.ReplyCommentRequest
import net.ntworld.mergeRequestIntegration.internal.CommentPositionImpl
import net.ntworld.mergeRequestIntegration.make
import net.ntworld.mergeRequestIntegration.provider.ProviderException
import net.ntworld.mergeRequestIntegrationIde.AbstractPresenter
import net.ntworld.mergeRequestIntegrationIde.diff.gutter.GutterActionType
import net.ntworld.mergeRequestIntegrationIde.diff.gutter.GutterIconRenderer
import net.ntworld.mergeRequestIntegrationIde.diff.gutter.GutterPosition
import net.ntworld.mergeRequestIntegrationIde.infrastructure.api.MergeRequestDataNotifier
import net.ntworld.mergeRequestIntegrationIde.service.ApplicationService
import net.ntworld.mergeRequestIntegrationIde.service.ProjectService
import net.ntworld.mergeRequestIntegrationIde.ui.editor.CommentPoint
import net.ntworld.mergeRequestIntegrationIde.ui.util.RepositoryUtil
import java.util.*

internal class DiffPresenterImpl(
    private val applicationService: ApplicationService,
    private val projectService: ProjectService,
    override val model: DiffModel,
    override val view: DiffView<*>
) : AbstractPresenter<EventListener>(), DiffPresenter, DiffView.ActionListener, DiffModel.DataListener {
    override val dispatcher = EventDispatcher.create(EventListener::class.java)

    init {
        view.addActionListener(this)
        model.addDataListener(this)
        Disposer.register(model, this)
    }

    override fun dispose() {
        view.dispose()
    }

    override fun onInit() {}
    override fun onDispose() {}
    override fun onBeforeRediff() {}

    override fun onAfterRediff() {
        view.createGutterIcons()
        val before = groupCommentsByLine(model.commentsOnBeforeSide)
        for (item in before) {
            view.changeGutterIconsByComments(item.key, DiffView.ContentType.BEFORE, item.value)
        }

        val after = groupCommentsByLine(model.commentsOnAfterSide)
        for (item in after) {
            view.changeGutterIconsByComments(item.key, DiffView.ContentType.AFTER, item.value)
        }
    }

    override fun onRediffAborted() {}

    override fun onCommentsUpdated() {
        view.resetGutterIcons()
        val before = groupCommentsByLine(model.commentsOnBeforeSide)
        for (item in before) {
            view.updateComments(
                model.providerData,
                model.mergeRequest,
                item.key,
                DiffView.ContentType.BEFORE,
                item.value
            )
        }

        val after = groupCommentsByLine(model.commentsOnAfterSide)
        for (item in after) {
            view.updateComments(
                model.providerData,
                model.mergeRequest,
                item.key,
                DiffView.ContentType.AFTER,
                item.value
            )
        }
    }

    override fun onGutterActionPerformed(renderer: GutterIconRenderer, type: GutterActionType) {
        when (type) {
            GutterActionType.ADD -> {
                view.displayEditorOnLine(
                    model.providerData,
                    model.mergeRequest,
                    renderer.logicalLine,
                    renderer.contentType,
                    collectCommentsOfGutterIconRenderer(renderer)
                )

            }
            GutterActionType.TOGGLE -> {
                view.toggleCommentsOnLine(
                    model.providerData,
                    model.mergeRequest,
                    renderer.logicalLine,
                    renderer.contentType,
                    collectCommentsOfGutterIconRenderer(renderer)
                )
            }
        }
    }

    override fun onReplyCommentRequested(
        content: String, repliedComment: Comment, logicalLine: Int, contentType: DiffView.ContentType
    ) {
        applicationService.infrastructure.serviceBus() process ReplyCommentRequest.make(
            providerId = model.providerData.id,
            mergeRequestId = model.mergeRequest.id,
            repliedComment = repliedComment,
            body = content
        ) ifError {
            projectService.notify(
                "There was an error from server. \n\n ${it.message}",
                NotificationType.ERROR
            )
            throw ProviderException(it)
        }
        fetchAndUpdateComments()
        view.resetEditor(logicalLine, contentType, repliedComment)
    }

    override fun onCreateCommentRequested(
        content: String, position: GutterPosition, logicalLine: Int, contentType: DiffView.ContentType
    ) {
        val commentPosition = convertGutterPositionToCommentPosition(position)
        applicationService.infrastructure.serviceBus() process CreateCommentRequest.make(
            providerId = model.providerData.id,
            mergeRequestId = model.mergeRequest.id,
            position = commentPosition,
            body = content
        ) ifError {
            projectService.notify(
                "There was an error from server. \n\n ${it.message}",
                NotificationType.ERROR
            )
            throw ProviderException(it)
        }
        fetchAndUpdateComments()
        view.resetEditor(logicalLine, contentType, null)
    }

    override fun onDeleteCommentRequested(comment: Comment) {
        applicationService.infrastructure.commandBus() process DeleteCommentCommand.make(
            providerId = model.providerData.id,
            mergeRequestId = model.mergeRequest.id,
            comment = comment
        )
        fetchAndUpdateComments()
    }

    override fun onResolveCommentRequested(comment: Comment) {
        applicationService.infrastructure.commandBus() process ResolveCommentCommand.make(
            providerId = model.providerData.id,
            mergeRequestId = model.mergeRequest.id,
            comment = comment
        )
        fetchAndUpdateComments()
    }

    override fun onUnresolveCommentRequested(comment: Comment) {
        applicationService.infrastructure.commandBus() process UnresolveCommentCommand.make(
            providerId = model.providerData.id,
            mergeRequestId = model.mergeRequest.id,
            comment = comment
        )
        fetchAndUpdateComments()
    }

    private fun fetchAndUpdateComments() {
        projectService.messageBus.syncPublisher(MergeRequestDataNotifier.TOPIC).fetchCommentsRequested(
            model.providerData, model.mergeRequest
        )
    }

    private fun collectCommentsOfGutterIconRenderer(renderer: GutterIconRenderer): List<Comment> {
        val result = mutableMapOf<String, CommentPoint>()
        model.commentsOnBeforeSide
            .filter { it.line == renderer.visibleLineLeft }
            .forEach { result[it.id] = it }
        model.commentsOnAfterSide
            .filter { it.line == renderer.visibleLineRight }
            .forEach { result[it.id] = it }

        return result.values.map { it.comment }
    }

    private fun groupCommentsByLine(commentPoints: List<CommentPoint>): Map<Int, List<Comment>> {
        val result = mutableMapOf<Int, MutableList<Comment>>()
        for (commentPoint in commentPoints) {
            if (!result.containsKey(commentPoint.line)) {
                result[commentPoint.line] = mutableListOf()
            }

            val list = result[commentPoint.line]!!
            list.add(commentPoint.comment)
        }
        return result
    }

    private fun convertGutterPositionToCommentPosition(input: GutterPosition): CommentPosition {
        val repository: GitRepository? = RepositoryUtil.findRepository(projectService.project, model.providerData)

        return CommentPositionImpl(
            oldLine = input.oldLine,
            oldPath = if (null === input.oldPath) null else RepositoryUtil.findRelativePath(repository, input.oldPath),
            newLine = input.newLine,
            newPath = if (null === input.newPath) null else RepositoryUtil.findRelativePath(repository, input.newPath),
            baseHash = if (input.baseHash.isNullOrEmpty()) findBaseHash() else input.baseHash,
            headHash = if (input.headHash.isNullOrEmpty()) findHeadHash() else input.headHash,
            startHash = if (input.startHash.isNullOrEmpty()) findStartHash() else input.startHash,
            source = when (input.editorType) {
                DiffView.EditorType.SINGLE_SIDE -> CommentPositionSource.SINGLE_SIDE
                DiffView.EditorType.TWO_SIDE_LEFT -> CommentPositionSource.SIDE_BY_SIDE_LEFT
                DiffView.EditorType.TWO_SIDE_RIGHT -> CommentPositionSource.SIDE_BY_SIDE_RIGHT
                DiffView.EditorType.UNIFIED -> CommentPositionSource.UNIFIED
            },
            changeType = when (input.changeType) {
                DiffView.ChangeType.UNKNOWN -> CommentPositionChangeType.UNKNOWN
                DiffView.ChangeType.INSERTED -> CommentPositionChangeType.INSERTED
                DiffView.ChangeType.DELETED -> CommentPositionChangeType.DELETED
                DiffView.ChangeType.MODIFIED -> CommentPositionChangeType.MODIFIED
            }
        )
    }

    private fun findBaseHash(): String {
        if (model.commits.isNotEmpty()) {
            return model.commits.last().id
        }

        val diff = model.mergeRequest.diffReference
        return if (null === diff) "" else diff.baseHash
    }

    private fun findStartHash(): String {
        val diff = model.mergeRequest.diffReference
        return if (null === diff) "" else diff.startHash
    }

    private fun findHeadHash(): String {
        if (model.commits.isNotEmpty()) {
            return model.commits.first().id
        }

        val diff = model.mergeRequest.diffReference
        return if (null === diff) "" else diff.headHash
    }
}