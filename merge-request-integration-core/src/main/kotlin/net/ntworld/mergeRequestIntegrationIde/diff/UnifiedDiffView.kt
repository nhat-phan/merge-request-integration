package net.ntworld.mergeRequestIntegrationIde.diff

import com.intellij.diff.tools.fragmented.UnifiedDiffViewer
import com.intellij.openapi.editor.markup.HighlighterLayer
import com.intellij.openapi.vcs.changes.Change
import gnu.trove.TIntFunction
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

    override fun createGutterIcons() {
        for (logicalLine in 0 until viewer.editor.document.lineCount) {
            val left = myLeftLineNumberConverter.execute(logicalLine)
            val right = myRightLineNumberConverter.execute(logicalLine)

            registerGutterIconRenderer(GutterIconRendererFactory.makeGutterIconRenderer(
                viewer.editor.markupModel.addLineHighlighter(logicalLine, HighlighterLayer.LAST, null),
                applicationService.settings.showAddCommentIconsInDiffViewGutter && (-1 != left || -1 != right),
                logicalLine,
                // TODO: calculate visible line for both side based on logical line and cache
                visibleLine = logicalLine + 1,
                // Doesn't matter, unified view only have 1 side
                contentType = DiffView.ContentType.BEFORE,
                action = this::onGutterIconActionTriggered
            ))
        }
    }

    private fun onGutterIconActionTriggered(renderer: GutterIconRenderer, actionType: GutterActionType) {
        when (actionType) {
            GutterActionType.ADD -> {
                dispatcher.multicaster.onAddGutterIconClicked(renderer, calcPosition(
                    renderer.visibleLine, renderer.logicalLine
                ))
            }
            GutterActionType.TOGGLE -> {
                dispatcher.multicaster.onCommentsGutterIconClicked(renderer)
            }
        }
    }

    override fun changeGutterIconsByComments(visibleLine: Int, contentType: DiffView.ContentType, comments: List<Comment>) {
        val map = if (contentType == DiffView.ContentType.BEFORE) myCachedLeftLineNumbers else myCachedRightLineNumbers
        val logicalLine = map[visibleLine - 1]
        if (null === logicalLine) {
            return
        }

        val oppositeContentType = if (contentType == DiffView.ContentType.BEFORE) DiffView.ContentType.AFTER else DiffView.ContentType.BEFORE
        if (!hasCommentsGutter(logicalLine, contentType) && !hasCommentsGutter(logicalLine, oppositeContentType)) {
            val lineHighlighter = viewer.editor.markupModel.addLineHighlighter(logicalLine, HighlighterLayer.LAST, null)
            lineHighlighter.gutterIconRenderer = CommentsGutterIconRenderer(
                visibleLine, logicalLine, contentType, dispatcher.multicaster::legacyOnCommentsGutterIconClicked
            )
        }
        registerCommentsGutter(logicalLine, contentType, comments)

        // Doesn't matter, unified view only have 1 side
        // see exact comment above
        val gutterIconRenderer = findGutterIconRenderer(logicalLine, DiffView.ContentType.BEFORE)
        gutterIconRenderer.setState(GutterState.COMMENTS_FROM_ONE_AUTHOR)
    }

    override fun toggleCommentsOnLine(
        providerData: ProviderData,
        mergeRequest: MergeRequest,
        visibleLine: Int,
        logicalLine: Int,
        contentType: DiffView.ContentType,
        comments: List<Comment>
    ) {
        // Note that do not use visibleLine because it's not correct for unified view,
        // it should be logicalLine + 1
        toggleCommentsOnLine(
            providerData,
            mergeRequest,
            viewer.editor,
            calcPosition(logicalLine + 1, logicalLine),
            logicalLine,
            contentType,
            comments
        )
    }

    private fun calcPosition(visibleLine: Int, logicalLine: Int): AddCommentRequestedPosition {
        return AddCommentRequestedPosition(
            editorType = DiffView.EditorType.UNIFIED,
            changeType = findChangeType(viewer.editor, logicalLine),
            oldLine = myLeftLineNumberConverter.execute(visibleLine),
            oldPath = change.beforeRevision!!.file.toString(),
            newLine = myRightLineNumberConverter.execute(visibleLine),
            newPath = change.afterRevision!!.file.toString(),
            baseHash = change.beforeRevision!!.revisionNumber.asString(),
            headHash = change.afterRevision!!.revisionNumber.asString()
        )
    }
}