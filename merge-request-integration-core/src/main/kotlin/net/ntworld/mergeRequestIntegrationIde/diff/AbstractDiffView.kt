package net.ntworld.mergeRequestIntegrationIde.diff

import com.intellij.diff.tools.util.base.DiffViewerBase
import com.intellij.diff.tools.util.base.DiffViewerListener
import com.intellij.diff.util.TextDiffType
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.util.Disposer
import com.intellij.util.EventDispatcher
import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequest.MergeRequest
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequestIntegrationIde.AbstractView
import net.ntworld.mergeRequestIntegrationIde.diff.gutter.GutterIconRenderer
import net.ntworld.mergeRequestIntegrationIde.diff.gutter.GutterPosition
import net.ntworld.mergeRequestIntegrationIde.diff.gutter.GutterState
import net.ntworld.mergeRequestIntegrationIde.diff.thread.*
import net.ntworld.mergeRequestIntegrationIde.service.ApplicationService

abstract class AbstractDiffView<V : DiffViewerBase>(
    private val applicationService: ApplicationService,
    private val viewerBase: DiffViewerBase
) : AbstractView<DiffView.ActionListener>(), DiffView<V> {
    final override val dispatcher = EventDispatcher.create(DiffView.ActionListener::class.java)
    private val diffViewerListener = object : DiffViewerListener() {
        override fun onInit() = dispatcher.multicaster.onInit()
        override fun onDispose() = dispatcher.multicaster.onDispose()
        override fun onBeforeRediff() = dispatcher.multicaster.onBeforeRediff()
        override fun onAfterRediff() = dispatcher.multicaster.onAfterRediff()
        override fun onRediffAborted() = dispatcher.multicaster.onRediffAborted()
    }
    private val myGutterIconRenderersOfBefore = mutableMapOf<Int, GutterIconRenderer>()
    private val myGutterIconRenderersOfAfter = mutableMapOf<Int, GutterIconRenderer>()

    private val myThreadModelOfBefore = mutableMapOf<Int, ThreadModel>()
    private val myThreadModelOfAfter = mutableMapOf<Int, ThreadModel>()
    private val myCommentEventPropagator = CommentEventPropagator(dispatcher)
    private val myThreadPresenterEventListener = object : ThreadPresenter.EventListener,
        CommentEvent by myCommentEventPropagator {
    }

    init {
        viewerBase.addListener(diffViewerListener)
    }

    override fun dispose() {
        myGutterIconRenderersOfBefore.clear()
        myGutterIconRenderersOfAfter.clear()
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

    protected fun toggleCommentsOnLine(
        providerData: ProviderData,
        mergeRequest: MergeRequest,
        editor: EditorEx,
        position: GutterPosition,
        logicalLine: Int,
        contentType: DiffView.ContentType,
        comments: List<Comment>
    ) {
        val model = findThreadModelOnLine(
            providerData, mergeRequest, editor, position, logicalLine, contentType, comments
        )
        model.visible = !model.visible
        setWritingStateOfGutterIconRenderer(model, logicalLine, contentType)
    }

    protected fun displayCommentsAndEditorOnLine(
        providerData: ProviderData,
        mergeRequest: MergeRequest,
        editor: EditorEx,
        position: GutterPosition,
        logicalLine: Int,
        contentType: DiffView.ContentType,
        comments: List<Comment>
    ) {
        val model = findThreadModelOnLine(
            providerData, mergeRequest, editor, position, logicalLine, contentType, comments
        )
        model.showEditor = true
        setWritingStateOfGutterIconRenderer(model, logicalLine, contentType)
    }

    private fun setWritingStateOfGutterIconRenderer(
        model: ThreadModel, logicalLine: Int, contentType: DiffView.ContentType
    ) {
        val renderer = findGutterIconRenderer(logicalLine, contentType)
        if (model.showEditor) {
            renderer.setState(GutterState.WRITING)
        }
    }

    private fun findThreadModelOnLine(
        providerData: ProviderData,
        mergeRequest: MergeRequest,
        editor: EditorEx,
        position: GutterPosition,
        logicalLine: Int,
        contentType: DiffView.ContentType,
        comments: List<Comment> = listOf()
    ): ThreadModel {
        val map = if (contentType == DiffView.ContentType.BEFORE) myThreadModelOfBefore else myThreadModelOfAfter
        if (!map.containsKey(logicalLine)) {
            val model = ThreadFactory.makeModel(comments)
            val view = ThreadFactory.makeView(editor, providerData, mergeRequest, logicalLine, position)
            val presenter = ThreadFactory.makePresenter(model, view)

            presenter.addListener(myThreadPresenterEventListener)
            Disposer.register(this, presenter)

            map[logicalLine] = model
        }
        return map[logicalLine]!!
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

    private fun makeGuessChangeTypeByColorFunction(editor: Editor): ((TextAttributes?) -> DiffView.ChangeType) {
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