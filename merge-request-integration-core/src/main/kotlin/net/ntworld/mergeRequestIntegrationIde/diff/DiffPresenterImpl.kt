package net.ntworld.mergeRequestIntegrationIde.diff

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.util.EventDispatcher
import git4idea.repo.GitRepository
import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequest.CommentPosition
import net.ntworld.mergeRequest.CommentPositionChangeType
import net.ntworld.mergeRequest.CommentPositionSource
import net.ntworld.mergeRequestIntegration.internal.CommentPositionImpl
import net.ntworld.mergeRequestIntegrationIde.diff.gutter.AddGutterIconRenderer
import net.ntworld.mergeRequestIntegrationIde.diff.gutter.CommentsGutterIconRenderer
import net.ntworld.mergeRequestIntegrationIde.internal.CommentStoreItem
import net.ntworld.mergeRequestIntegrationIde.service.ApplicationService
import net.ntworld.mergeRequestIntegrationIde.service.ProjectService
import net.ntworld.mergeRequestIntegrationIde.ui.editor.CommentPoint
import net.ntworld.mergeRequestIntegrationIde.ui.util.RepositoryUtil
import com.intellij.openapi.project.Project as IdeaProject
import java.util.*

internal class DiffPresenterImpl(
    private val projectService: ProjectService,
    override val model: DiffModel,
    override val view: DiffView<*>
) : DiffPresenter, DiffView.Action {
    override val dispatcher = EventDispatcher.create(EventListener::class.java)

    init {
        view.dispatcher.addListener(this)
    }

    override fun onInit() {}

    override fun onDispose() {}

    override fun onBeforeRediff() {}

    override fun onAfterRediff() = assertDoingCodeReview {
        view.initialize()
        displayGutterIcons()
        view.displayAddGutterIcons()
    }

    override fun onRediffAborted() {}

    override fun onAddGutterIconClicked(renderer: AddGutterIconRenderer, position: AddCommentRequestedPosition) {
        val commentPosition = convertAddCommentRequestedPositionToCommentPosition(position)
        val providerData = model.providerData!!
        val mergeRequest = model.mergeRequest!!
        val item = CommentStoreItem.createNewItem(
            providerData, mergeRequest,
            projectService.codeReviewUtil.convertPositionToCommentNodeData(commentPosition),
            commentPosition
        )
        projectService.commentStore.add(item)
        projectService.dispatcher.multicaster.newCommentRequested(
            providerData, mergeRequest, commentPosition, item
        )
    }

    override fun onCommentsGutterIconClicked(renderer: CommentsGutterIconRenderer, e: AnActionEvent) {
        assertDoingCodeReview {
            val bucket = if (renderer.contentType == DiffView.ContentType.BEFORE) model.commentsOnBeforeSide else model.commentsOnAfterSide
            val comments = bucket
                .filter { it.line == renderer.visibleLine }
                .map { it.comment }

            view.displayCommentsOnLine(
                model.providerData!!,
                renderer.visibleLine,
                renderer.logicalLine,
                renderer.contentType,
                comments
            )
        }
    }

    private fun displayGutterIcons() {
        val before = groupCommentsByLine(model.commentsOnBeforeSide)
        for (item in before) {
            view.displayCommentsGutterIcon(item.key, DiffView.ContentType.BEFORE, item.value)
        }

        val after = groupCommentsByLine(model.commentsOnAfterSide)
        for (item in after) {
            view.displayCommentsGutterIcon(item.key, DiffView.ContentType.AFTER, item.value)
        }
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

    private fun assertDoingCodeReview(invoker: (() -> Unit)) {
        if (null !== model.mergeRequest && null !== model.providerData) {
            invoker.invoke()
        }
    }

    private fun convertAddCommentRequestedPositionToCommentPosition(input: AddCommentRequestedPosition): CommentPosition {
        val repository: GitRepository? = RepositoryUtil.findRepository(projectService.project, model.providerData!!)

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

        val diff = model.mergeRequest!!.diffReference
        return if (null === diff) "" else diff.baseHash
    }

    private fun findStartHash(): String {
        val diff = model.mergeRequest!!.diffReference
        return if (null === diff) "" else diff.startHash
    }

    private fun findHeadHash(): String {
        if (model.commits.isNotEmpty()) {
            return model.commits.first().id
        }

        val diff = model.mergeRequest!!.diffReference
        return if (null === diff) "" else diff.headHash
    }
}