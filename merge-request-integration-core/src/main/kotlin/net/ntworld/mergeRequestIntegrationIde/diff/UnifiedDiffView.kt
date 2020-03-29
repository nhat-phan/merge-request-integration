package net.ntworld.mergeRequestIntegrationIde.diff

import com.intellij.diff.tools.fragmented.UnifiedDiffViewer
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.markup.HighlighterLayer
import com.intellij.openapi.vcs.changes.Change
import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequest.MergeRequest
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequestIntegrationIde.diff.gutter.*
import net.ntworld.mergeRequestIntegrationIde.service.ApplicationService

class UnifiedDiffView(
    private val applicationService: ApplicationService,
    override val viewer: UnifiedDiffViewer,
    private val change: Change
) : AbstractDiffView<UnifiedDiffViewer>(applicationService, viewer) {
    private val myLeftLineNumberConverter by lazy {
        applicationService.intellijIdeApi.findLeftLineNumberConverter(viewer.editor)
    }
    private val myRightLineNumberConverter by lazy {
        applicationService.intellijIdeApi.findRightLineNumberConverter(viewer.editor)
    }
    private val myCachedLeftLineNumbers = mutableMapOf<Int, Int>()
    private val myCachedRightLineNumbers = mutableMapOf<Int, Int>()

    override fun convertVisibleLineToLogicalLine(visibleLine: Int, contentType: DiffView.ContentType): Int {
        val map = if (contentType == DiffView.ContentType.BEFORE) myCachedLeftLineNumbers else myCachedRightLineNumbers
        val logicalLine = map[visibleLine - 1]
        if (null === logicalLine) {
            return -1
        }
        return logicalLine
    }

    override fun createGutterIcons() {
        for (logicalLine in 0 until viewer.editor.document.lineCount) {
            val left = myLeftLineNumberConverter.execute(logicalLine)
            val right = myRightLineNumberConverter.execute(logicalLine)
            myCachedLeftLineNumbers[left] = logicalLine
            myCachedRightLineNumbers[right] = logicalLine

            registerGutterIconRenderer(
                GutterIconRendererFactory.makeGutterIconRenderer(
                    viewer.editor.markupModel.addLineHighlighter(logicalLine, HighlighterLayer.LAST, null),
                    applicationService.settings.showAddCommentIconsInDiffViewGutter && (-1 != left || -1 != right),
                    logicalLine,
                    visibleLineLeft = left + 1,
                    visibleLineRight = right + 1,
                    // Doesn't matter, unified view only have 1 side
                    contentType = DiffView.ContentType.BEFORE,
                    action = ::dispatchOnGutterActionPerformed
                )
            )
        }
    }

    private fun findGutterIconRenderer(
        visibleLine: Int,
        contentType: DiffView.ContentType,
        invoker: ((Int, GutterIconRenderer) -> Unit)
    ) {
        val logicalLine = convertVisibleLineToLogicalLine(visibleLine, contentType)
        if (-1 == logicalLine) {
            return
        }

        // Doesn't matter, unified view only have 1 side
        // see exact comment above
        invoker.invoke(logicalLine, findGutterIconRenderer(logicalLine, DiffView.ContentType.BEFORE))
    }

    override fun changeGutterIconsByComments(
        visibleLine: Int,
        contentType: DiffView.ContentType,
        comments: List<Comment>
    ) {
        findGutterIconRenderer(visibleLine, contentType) {_, renderer ->
            updateGutterIcon(renderer, comments)
        }
    }

    override fun updateComments(
        providerData: ProviderData,
        mergeRequest: MergeRequest,
        visibleLine: Int,
        contentType: DiffView.ContentType,
        comments: List<Comment>,
        requestSource: DiffModel.Source
    ) {
        findGutterIconRenderer(visibleLine, contentType) { logicalLine, renderer ->
            updateComments(
                providerData,
                mergeRequest,
                viewer.editor,
                calcPosition(logicalLine),
                renderer,
                comments
            )
        }
    }

    override fun displayEditorOnLine(
        providerData: ProviderData,
        mergeRequest: MergeRequest,
        logicalLine: Int,
        contentType: DiffView.ContentType,
        comments: List<Comment>
    ) {
        displayCommentsAndEditorOnLine(
            providerData, mergeRequest,
            viewer.editor,
            calcPosition(logicalLine),
            logicalLine, contentType,
            comments
        )
    }

    override fun changeCommentsVisibilityOnLine(
        providerData: ProviderData,
        mergeRequest: MergeRequest,
        logicalLine: Int,
        contentType: DiffView.ContentType,
        comments: List<Comment>,
        mode: DiffView.DisplayCommentMode
    ) {
        toggleCommentsOnLine(
            providerData, mergeRequest,
            viewer.editor,
            calcPosition(logicalLine),
            logicalLine, contentType,
            comments,
            mode
        )
    }

    private fun calcPosition(logicalLine: Int): GutterPosition {
        val left = myLeftLineNumberConverter.execute(logicalLine) + 1
        val right = myRightLineNumberConverter.execute(logicalLine) + 1
        return GutterPosition(
            editorType = DiffView.EditorType.UNIFIED,
            changeType = findChangeType(viewer.editor, logicalLine),
            oldLine = if (left > 0) left else -1,
            oldPath = change.beforeRevision!!.file.toString(),
            newLine = if (right > 0) right else -1,
            newPath = change.afterRevision!!.file.toString(),
            baseHash = change.beforeRevision!!.revisionNumber.asString(),
            headHash = change.afterRevision!!.revisionNumber.asString()
        )
    }
}