package net.ntworld.mergeRequestIntegrationIde.diff

import com.intellij.util.EventDispatcher
import git4idea.repo.GitRepository
import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequest.CommentPosition
import net.ntworld.mergeRequest.CommentPositionChangeType
import net.ntworld.mergeRequest.CommentPositionSource
import net.ntworld.mergeRequestIntegration.internal.CommentPositionImpl
import net.ntworld.mergeRequestIntegrationIde.diff.gutter.GutterIconRenderer
import net.ntworld.mergeRequestIntegrationIde.internal.CommentStoreItem
import net.ntworld.mergeRequestIntegrationIde.service.ProjectService
import net.ntworld.mergeRequestIntegrationIde.ui.editor.CommentPoint
import net.ntworld.mergeRequestIntegrationIde.ui.util.RepositoryUtil
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

    override fun onDispose() {
        view.dispose()
    }

    override fun onBeforeRediff() {}

    override fun onAfterRediff() = assertDoingCodeReview {
        view.createGutterIcons()
        displayGutterIcons()
    }

    override fun onRediffAborted() {}

    override fun onAddGutterIconClicked(renderer: GutterIconRenderer, position: AddCommentRequestedPosition) {
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

    override fun onCommentsGutterIconClicked(renderer: GutterIconRenderer) {
        assertDoingCodeReview {
            val result = mutableMapOf<String, CommentPoint>()
            model.commentsOnBeforeSide
                .filter { it.line == renderer.visibleLineLeft }
                .forEach { result[it.id] = it }
            model.commentsOnAfterSide
                .filter { it.line == renderer.visibleLineRight }
                .forEach { result[it.id] = it }

            val comments = result.values.map { it.comment }
            view.toggleCommentsOnLine(
                model.providerData!!,
                model.mergeRequest!!,
                renderer.logicalLine,
                renderer.contentType,
                comments
            )
        }
    }

    private fun displayGutterIcons() {
        val before = groupCommentsByLine(model.commentsOnBeforeSide)
        for (item in before) {
            view.changeGutterIconsByComments(item.key, DiffView.ContentType.BEFORE, item.value)
        }

        val after = groupCommentsByLine(model.commentsOnAfterSide)
        for (item in after) {
            view.changeGutterIconsByComments(item.key, DiffView.ContentType.AFTER, item.value)
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