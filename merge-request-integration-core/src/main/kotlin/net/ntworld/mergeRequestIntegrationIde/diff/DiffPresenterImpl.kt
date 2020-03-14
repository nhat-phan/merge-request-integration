package net.ntworld.mergeRequestIntegrationIde.diff

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.util.EventDispatcher
import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequestIntegrationIde.diff.gutter.AddGutterIconRenderer
import net.ntworld.mergeRequestIntegrationIde.diff.gutter.CommentsGutterIconRenderer
import net.ntworld.mergeRequestIntegrationIde.ui.editor.CommentPoint
import java.util.*

internal class DiffPresenterImpl(
    override val model: DiffModel,
    override val view: DiffView<*>
) : DiffPresenter, DiffView.Action {
    override val dispatcher = EventDispatcher.create(EventListener::class.java)

    init {
        view.dispatcher.addListener(this)
    }

    override fun onInit() {
    }

    override fun onDispose() {
    }

    override fun onBeforeRediff() {
    }

    override fun onAfterRediff() {
        view.displayAddGutterIcons()
        displayGutterIcons()
    }

    override fun onRediffAborted() {
    }

    override fun onAddGutterIconClicked(renderer: AddGutterIconRenderer, e: AnActionEvent?) {
        println("Request add a comment on line: ${renderer.visibleLine}")
    }

    override fun onCommentsGutterIconClicked(renderer: CommentsGutterIconRenderer, e: AnActionEvent) {
        println("Display comments on line: ${renderer.visibleLine}")
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

}