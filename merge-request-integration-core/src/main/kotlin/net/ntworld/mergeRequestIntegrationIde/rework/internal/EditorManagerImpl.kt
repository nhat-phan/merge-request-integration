package net.ntworld.mergeRequestIntegrationIde.rework.internal

import com.intellij.diff.util.Side
import com.intellij.openapi.editor.markup.HighlighterLayer
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.vcs.changes.Change
import com.intellij.openapi.vfs.VirtualFile
import net.ntworld.mergeRequestIntegrationIde.diff.gutter.GutterActionType
import net.ntworld.mergeRequestIntegrationIde.diff.gutter.GutterIconRenderer
import net.ntworld.mergeRequestIntegrationIde.diff.gutter.GutterIconRendererFactory
import net.ntworld.mergeRequestIntegrationIde.infrastructure.ProjectServiceProvider
import net.ntworld.mergeRequestIntegrationIde.rework.EditorManager
import net.ntworld.mergeRequestIntegrationIde.rework.ReworkWatcher
import net.ntworld.mergeRequestIntegrationIde.util.CommentUtil
import java.util.*

class EditorManagerImpl(
    private val projectServiceProvider: ProjectServiceProvider
) : EditorManager {
    // TODO: set to false & remove me after debug
    private val shouldDisplayAddIcon = true
    private val myInitializedEditors = Collections.synchronizedMap(mutableMapOf<TextEditor, ReworkWatcher>())

    override fun initialize(textEditor: TextEditor, watcher: ReworkWatcher) {
        if (myInitializedEditors.containsKey(textEditor)) {
            return
        }
        val virtualFile = textEditor.file
        if (null === virtualFile) {
            return
        }
        println(virtualFile.path)
        val change = findChangeForVirtualFile(watcher, virtualFile)
        if (null === change) {
            return
        }

        val editor = textEditor.editor
        val lineCount = editor.document.lineCount
        // register an GutterIconRenderer to all line
        for (logicalLine in 0 until lineCount) {
            GutterIconRendererFactory.makeGutterIconRenderer(
                editor.markupModel.addLineHighlighter(logicalLine, HighlighterLayer.LAST, null),
                shouldDisplayAddIcon,
                logicalLine,
                visibleLineLeft = null,
                visibleLineRight = logicalLine + 1,
                side = Side.RIGHT,
                action = ::dispatchOnGutterActionPerformed
            )
        }

        // val comments = CommentUtil.groupCommentsByNewPath(watcher.comments)
        // println(comments)

        // myInitializedEditors[textEditor] = watcher
        // val comments = watcher.comments

        // val editor = textEditor.editor
        // editor.markupModel.addLineHighlighter(1, HighlighterLayer.LAST, null)
        myInitializedEditors[textEditor] = watcher
    }

    private fun dispatchOnGutterActionPerformed(renderer: GutterIconRenderer, type: GutterActionType) {
        println(renderer)
        println(type)
    }

    private fun findChangeForVirtualFile(watcher: ReworkWatcher, virtualFile: VirtualFile): Change? {
        val changes = watcher.changes
        for (change in changes) {
            val afterRevision = change.afterRevision
            if (null === afterRevision) {
                continue
            }
            val path = afterRevision.file.path
            if (virtualFile.path == path) {
                return change
            }
        }
        return null
    }

    override fun shutdown(textEditor: TextEditor) {
        if (!myInitializedEditors.contains(textEditor)) {
            return
        }

        val makeupModel = textEditor.editor.markupModel
        val highlighters = makeupModel.allHighlighters
        for (highlighter in highlighters) {
            if (highlighter.gutterIconRenderer is GutterIconRenderer) {
                makeupModel.removeHighlighter(highlighter)
                highlighter.dispose()
            }
        }
        myInitializedEditors.remove(textEditor)
    }
}