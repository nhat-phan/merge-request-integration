package net.ntworld.mergeRequestIntegrationIde.ui.editor

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.editor.impl.DocumentMarkupModel
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.util.Key
import net.ntworld.mergeRequestIntegrationIde.service.ApplicationService
import net.ntworld.mergeRequestIntegrationIde.service.ProjectService
import net.ntworld.mergeRequestIntegrationIde.ui.util.Icons
import javax.swing.Icon

object EditorCommentManager {
    private val COMMENT_POINTS_KEY = Key<MutableMap<String, CommentPoint>?>("MRI-CommentPoints")
    private val LINE_THREADS_KEY = Key<MutableMap<Int, MutableList<String>>?>("MRI-LineThreads")


    fun createPoint(applicationService: ApplicationService, editor: EditorEx, point: CommentPoint) {
        val ideaProject = editor.project
        val commentData = findCommentPointsData(editor)
        if (null === ideaProject || commentData.containsKey(point.id)) {
            return
        }

        val lineThreadGroupedData = findLineThreadGroupedData(editor)
        val threadsInLine = lineThreadGroupedData[point.line] ?: mutableListOf()
        if (!threadsInLine.contains(point.comment.parentId)) {
            val document = editor.document
            val makeupModel = DocumentMarkupModel.forDocument(document, ideaProject, true)
            val highlighter = makeupModel.addLineHighlighter(point.line, 0, null)
            highlighter.gutterIconRenderer = MyCommentGutterIconRenderer(applicationService, point)

            threadsInLine.add(point.comment.parentId)
        }

        lineThreadGroupedData[point.line] = threadsInLine
        commentData[point.id] = point
        editor.putUserData(COMMENT_POINTS_KEY, commentData)
        editor.putUserData(LINE_THREADS_KEY, lineThreadGroupedData)
    }

    private fun findCommentPointsData(editor: EditorEx): MutableMap<String, CommentPoint> {
        val data = editor.getUserData(COMMENT_POINTS_KEY)
        return if (null === data) mutableMapOf() else data
    }

    private fun findLineThreadGroupedData(editor: EditorEx): MutableMap<Int, MutableList<String>> {
        val data = editor.getUserData(LINE_THREADS_KEY)
        return if (null === data) mutableMapOf() else data
    }

    private class MyCommentGutterIconRenderer(
        private val applicationService: ApplicationService,
        private val point: CommentPoint
    ): GutterIconRenderer() {
        private val myClickAction = object : AnAction() {
            override fun actionPerformed(e: AnActionEvent) {
                val project = e.project
                if (null !== project) {
                    applicationService.getProjectService(project).dispatcher.multicaster.displayCommentRequested(
                        point.comment
                    )
                }
            }
        }

        override fun hashCode(): Int {
            return System.identityHashCode(this)
        }

        override fun getIcon(): Icon {
            return Icons.HasComment
        }

        override fun equals(other: Any?): Boolean {
            return other === this
        }

        override fun getClickAction(): AnAction? = myClickAction

        override fun getTooltipText(): String? {
            return "Click to view the comment"
        }

        override fun isNavigateAction(): Boolean {
            return true
        }
    }
}