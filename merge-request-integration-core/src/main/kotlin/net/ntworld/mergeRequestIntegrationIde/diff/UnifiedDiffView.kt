package net.ntworld.mergeRequestIntegrationIde.diff

import com.intellij.diff.tools.fragmented.UnifiedDiffViewer
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.editor.markup.HighlighterLayer
import com.intellij.openapi.vcs.changes.Change
import gnu.trove.TIntFunction
import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequestIntegrationIde.diff.gutter.AddGutterIconRenderer
import net.ntworld.mergeRequestIntegrationIde.diff.gutter.CommentsGutterIconRenderer
import net.ntworld.mergeRequestIntegrationIde.service.ApplicationService

class UnifiedDiffView(
    private val applicationService: ApplicationService,
    override val viewer: UnifiedDiffViewer,
    private val change: Change
) : AbstractDiffView<UnifiedDiffViewer>(viewer) {
    private val myLeftLineNumberConverter by lazy {
        try {
            val myLineNumberConvertor = viewer.editor.gutter.javaClass.getDeclaredField("myLineNumberConvertor")
            myLineNumberConvertor.isAccessible = true
            myLineNumberConvertor.get(viewer.editor.gutter) as TIntFunction
        } catch (exception: NoSuchFieldException) {
            val myLineNumberConverter = viewer.editor.gutter.javaClass.getDeclaredField("myLineNumberConverter")
            myLineNumberConverter.isAccessible = true
            myLineNumberConverter.get(viewer.editor.gutter) as TIntFunction
        }
    }
    private val myRightLineNumberConverter by lazy {
        try {
            val myAdditionalLineNumberConvertor = viewer.editor.gutter.javaClass.getDeclaredField(
                "myAdditionalLineNumberConvertor"
            )
            myAdditionalLineNumberConvertor.isAccessible = true
            myAdditionalLineNumberConvertor.get(viewer.editor.gutter) as TIntFunction
        } catch (exception: NoSuchFieldException) {
            val myAdditionalLineNumberConverter = viewer.editor.gutter.javaClass.getDeclaredField(
                "myAdditionalLineNumberConverter"
            )
            myAdditionalLineNumberConverter.isAccessible = true
            myAdditionalLineNumberConverter.get(viewer.editor.gutter) as TIntFunction
        }
    }
    private val myCachedLeftLineNumbers = mutableMapOf<Int, Int>()
    private val myCachedRightLineNumbers = mutableMapOf<Int, Int>()

    override fun initialize() {
        for (line in 0 until viewer.editor.document.lineCount) {
            val left = myLeftLineNumberConverter.execute(line)
            val right = myRightLineNumberConverter.execute(line)
            myCachedLeftLineNumbers[left] = line
            myCachedRightLineNumbers[right] = line
        }
    }

    override fun displayAddGutterIcons() {
        for (logicalLine in 0 until viewer.editor.document.lineCount) {
            val left = myLeftLineNumberConverter.execute(logicalLine)
            val right = myRightLineNumberConverter.execute(logicalLine)
            if (-1 == left && -1 == right) {
                continue
            }

            val lineHighlighter = viewer.editor.markupModel.addLineHighlighter(logicalLine, HighlighterLayer.LAST, null)
            lineHighlighter.gutterIconRenderer = AddGutterIconRenderer(
                applicationService.settings.showAddCommentIconsInDiffViewGutter &&
                    !hasCommentsGutter(logicalLine, DiffView.ContentType.BEFORE) &&
                    !hasCommentsGutter(logicalLine, DiffView.ContentType.AFTER),
                logicalLine + 1,
                logicalLine,
                this::onAddGutterIconClicked
            )
        }
    }

    private fun onAddGutterIconClicked(renderer: AddGutterIconRenderer, e: AnActionEvent?) {
        dispatcher.multicaster.onAddGutterIconClicked(renderer, AddCommentRequestedPosition(
            oldLine = myLeftLineNumberConverter.execute(renderer.visibleLine),
            oldPath = change.beforeRevision!!.file.toString(),
            newLine = myRightLineNumberConverter.execute(renderer.visibleLine),
            newPath = change.afterRevision!!.file.toString(),
            baseHash = change.beforeRevision!!.revisionNumber.asString(),
            headHash = change.afterRevision!!.revisionNumber.asString()
        ))
    }

    override fun displayCommentsGutterIcon(line: Int, contentType: DiffView.ContentType, comments: List<Comment>) {
        val map = if (contentType == DiffView.ContentType.BEFORE) myCachedLeftLineNumbers else myCachedRightLineNumbers
        val logicalLine = map[line - 1]
        if (null === logicalLine) {
            return
        }

        val oppositeContentType = if (contentType == DiffView.ContentType.BEFORE) DiffView.ContentType.AFTER else DiffView.ContentType.BEFORE
        if (!hasCommentsGutter(logicalLine, contentType) && !hasCommentsGutter(logicalLine, oppositeContentType)) {
            val lineHighlighter = viewer.editor.markupModel.addLineHighlighter(logicalLine, HighlighterLayer.LAST, null)
            lineHighlighter.gutterIconRenderer = CommentsGutterIconRenderer(
                line, logicalLine, dispatcher.multicaster::onCommentsGutterIconClicked
            )
        }
        registerCommentsGutter(logicalLine, contentType, comments)
    }

    override fun hideComments() {
    }
}