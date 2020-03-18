package net.ntworld.mergeRequestIntegrationIde.diff

import com.intellij.diff.tools.util.base.DiffViewerBase
import com.intellij.diff.tools.util.base.DiffViewerListener
import com.intellij.diff.util.TextDiffType
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.util.EventDispatcher
import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequest.MergeRequest
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequestIntegrationIde.diff.gutter.GutterIconRenderer
import net.ntworld.mergeRequestIntegrationIde.diff.thread.ThreadFactory
import net.ntworld.mergeRequestIntegrationIde.diff.thread.ThreadModel
import net.ntworld.mergeRequestIntegrationIde.service.ApplicationService

abstract class AbstractDiffView<V : DiffViewerBase>(
    private val applicationService: ApplicationService,
    private val viewerBase: DiffViewerBase
) : DiffView<V> {
    private val diffViewerListener = object : DiffViewerListener() {
        override fun onInit() = dispatcher.multicaster.onInit()
        override fun onDispose() = dispatcher.multicaster.onDispose()
        override fun onBeforeRediff() = dispatcher.multicaster.onBeforeRediff()
        override fun onAfterRediff() = dispatcher.multicaster.onAfterRediff()
        override fun onRediffAborted() = dispatcher.multicaster.onRediffAborted()
    }
    private val myGutterIconRenderersOfBefore = mutableMapOf<Int, GutterIconRenderer>()
    private val myGutterIconRenderersOfAfter = mutableMapOf<Int, GutterIconRenderer>()

    private val myCommentsGutterOfBefore = mutableMapOf<Int, MutableSet<String>>()
    private val myCommentsGutterOfAfter = mutableMapOf<Int, MutableSet<String>>()
    private val myThreadModelOfBefore = mutableMapOf<Int, ThreadModel>()
    private val myThreadModelOfAfter = mutableMapOf<Int, ThreadModel>()

    override val dispatcher = EventDispatcher.create(DiffView.Action::class.java)

    init {
        viewerBase.addListener(diffViewerListener)
    }

    override fun initialize() {
    }

    protected fun registerGutterIconRenderer(gutterIconRenderer: GutterIconRenderer) {
        val map = if (gutterIconRenderer.contentType == DiffView.ContentType.BEFORE)
            myGutterIconRenderersOfBefore else myGutterIconRenderersOfAfter

        map[gutterIconRenderer.logicalLine] = gutterIconRenderer
    }

    protected fun findGutterIconRenderer(logicalLine: Int, contentType: DiffView.ContentType): GutterIconRenderer {
        val map = if (contentType == DiffView.ContentType.BEFORE)
            myGutterIconRenderersOfBefore else myGutterIconRenderersOfAfter

        return map[logicalLine]!!
    }

    protected fun registerCommentsGutter(line: Int, contentType: DiffView.ContentType, comments: List<Comment>) {
        val map = if (contentType == DiffView.ContentType.BEFORE) myCommentsGutterOfBefore else myCommentsGutterOfAfter
        val ids = map[line]
        if (null === ids) {
            map[line] = collectThreadIds(comments).toMutableSet()
        } else {
            ids.addAll(collectThreadIds(comments))
        }
    }

    protected fun hasCommentsGutter(line: Int, contentType: DiffView.ContentType): Boolean {
        val map = if (contentType == DiffView.ContentType.BEFORE) myCommentsGutterOfBefore else myCommentsGutterOfAfter

        return map.containsKey(line)
    }

    protected fun toggleCommentsOnLine(
        providerData: ProviderData,
        mergeRequest: MergeRequest,
        editor: EditorEx,
        position: AddCommentRequestedPosition,
        logicalLine: Int,
        contentType: DiffView.ContentType,
        comments: List<Comment>
    ) {
        val map = if (contentType == DiffView.ContentType.BEFORE) myThreadModelOfBefore else myThreadModelOfAfter
        if (!map.containsKey(logicalLine)) {
            val model = ThreadFactory.makeModel(comments)
            val view = ThreadFactory.makeView(editor, providerData, mergeRequest, logicalLine, position)
            val presenter = ThreadFactory.makePresenter(model, view)

            map[logicalLine] = model
        } else {
            val model = map[logicalLine]!!
            model.visible = !model.visible
        }
    }

    protected fun findChangeType(editor: EditorEx, logicalLine: Int): DiffView.ChangeType {
        val guessChangeTypeByColorFunction = makeGuessChangeTypeByColorFunction(editor)
        val highlighters = editor.markupModel.allHighlighters
        var type = DiffView.ChangeType.UNKNOWN
        for (highlighter in highlighters) {
            val startLogicalPosition = editor.offsetToLogicalPosition(highlighter.startOffset)
            val endLogicalPosition = editor.offsetToLogicalPosition(highlighter.endOffset)
            if (startLogicalPosition.line > logicalLine || logicalLine > endLogicalPosition.line) {
                continue
            }

            val guessType = guessChangeTypeByColorFunction(highlighter.textAttributes)
            if (guessType != DiffView.ChangeType.UNKNOWN) {
                type = guessType
            }
        }
        return type
    }

    private fun collectThreadIds(comments: List<Comment>): MutableSet<String> {
        val result = mutableSetOf<String>()
        comments.forEach {
            result.add(it.parentId)
        }
        return result
    }

    companion object {
        @JvmStatic
        fun makeGuessChangeTypeByColorFunction(editor: Editor): ((TextAttributes?) -> DiffView.ChangeType) {
            val insertedColor = TextDiffType.INSERTED.getColor(editor).rgb
            val insertedIgnoredColor = TextDiffType.INSERTED.getIgnoredColor(editor).rgb
            val deletedColor = TextDiffType.DELETED.getColor(editor).rgb
            val deletedIgnoredColor = TextDiffType.DELETED.getIgnoredColor(editor).rgb
            val modifiedColor = TextDiffType.MODIFIED.getColor(editor).rgb
            val modifiedIgnoredColor = TextDiffType.MODIFIED.getIgnoredColor(editor).rgb

            return {
                if (null === it) {
                    DiffView.ChangeType.UNKNOWN
                } else {
                    val bgColor = it.backgroundColor
                    if (null === bgColor) {
                        DiffView.ChangeType.UNKNOWN
                    } else {
                        when (bgColor.rgb) {
                            insertedColor, insertedIgnoredColor -> DiffView.ChangeType.INSERTED
                            modifiedColor, modifiedIgnoredColor -> DiffView.ChangeType.MODIFIED
                            deletedColor, deletedIgnoredColor -> DiffView.ChangeType.DELETED
                            else -> DiffView.ChangeType.UNKNOWN
                        }
                    }
                }
            }
        }

    }
}