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
import net.ntworld.mergeRequest.MergeRequestInfo
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequestIntegrationIde.AbstractView
import net.ntworld.mergeRequestIntegrationIde.diff.gutter.GutterActionType
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
        override fun onMainEditorClosed(threadPresenter: ThreadPresenter) {
            val renderer = findGutterIconRenderer(threadPresenter.view.logicalLine, threadPresenter.view.contentType)
            if (threadPresenter.model.comments.isEmpty()) {
                renderer.setState(GutterState.NO_COMMENT)
            } else {
                renderer.setState(
                    if (threadPresenter.model.comments.size == 1)
                        GutterState.THREAD_HAS_SINGLE_COMMENT
                    else
                        GutterState.THREAD_HAS_MULTI_COMMENTS
                )
            }
        }

        override fun onReplyCommentRequested(
            content: String, repliedComment: Comment, logicalLine: Int, contentType: DiffView.ContentType
        ) {
            dispatcher.multicaster.onReplyCommentRequested(content, repliedComment, logicalLine, contentType)
        }

        override fun onCreateCommentRequested(
            content: String, position: GutterPosition, logicalLine: Int, contentType: DiffView.ContentType
        ) {
            dispatcher.multicaster.onCreateCommentRequested(content, position, logicalLine, contentType)
        }
    }

    init {
        viewerBase.addListener(diffViewerListener)
    }

    protected abstract fun convertVisibleLineToLogicalLine(visibleLine: Int, contentType: DiffView.ContentType): Int

    override fun destroyExistingComments(excludedVisibleLines: Set<Int>, contentType: DiffView.ContentType) {
        val excludedLogicalLines = excludedVisibleLines
            .map { convertVisibleLineToLogicalLine(it, contentType) }
            .filter { it >= 0 }

        val map = if (contentType == DiffView.ContentType.BEFORE) myThreadModelOfBefore else myThreadModelOfAfter
        val removedKeys = mutableListOf<Int>()
        for (entry in map) {
            if (excludedLogicalLines.contains(entry.key)) {
                continue
            }
            entry.value.comments = listOf()
            removedKeys.add(entry.key)
        }
        removedKeys.forEach { map.remove(it) }
    }

    override fun showAllComments() = changeAllCommentsVisibility(true)

    override fun hideAllComments() = changeAllCommentsVisibility(false)

    override fun resetGutterIcons() {
        for (renderer in myGutterIconRenderersOfBefore.values) {
            renderer.setState(GutterState.NO_COMMENT)
        }
        for (renderer in myGutterIconRenderersOfAfter.values) {
            renderer.setState(GutterState.NO_COMMENT)
        }
    }

    override fun resetEditor(logicalLine: Int, contentType: DiffView.ContentType, repliedComment: Comment?) {
        val map = if (contentType == DiffView.ContentType.BEFORE)
            myThreadModelOfBefore else myThreadModelOfAfter

        val model = map[logicalLine]
        if (null !== model) {
            model.resetEditor(repliedComment)
        }
    }

    override fun dispose() {
        myGutterIconRenderersOfBefore.clear()
        myGutterIconRenderersOfAfter.clear()
    }

    protected fun dispatchOnGutterActionPerformed(renderer: GutterIconRenderer, type: GutterActionType) {
        dispatcher.multicaster.onGutterActionPerformed(renderer, type, DiffView.DisplayCommentMode.TOGGLE)
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
        mergeRequestInfo: MergeRequestInfo,
        editor: EditorEx,
        position: GutterPosition,
        logicalLine: Int,
        contentType: DiffView.ContentType,
        comments: List<Comment>,
        mode: DiffView.DisplayCommentMode
    ) {
        val model = findThreadModelOnLine(
            providerData, mergeRequestInfo, editor, position, logicalLine, contentType, comments
        )
        when (mode) {
            DiffView.DisplayCommentMode.TOGGLE -> model.visible = !model.visible
            DiffView.DisplayCommentMode.SHOW -> model.visible = true
            DiffView.DisplayCommentMode.HIDE -> model.visible = false
        }
        setWritingStateOfGutterIconRenderer(model, logicalLine, contentType)
    }

    protected fun displayCommentsAndEditorOnLine(
        providerData: ProviderData,
        mergeRequestInfo: MergeRequestInfo,
        editor: EditorEx,
        position: GutterPosition,
        logicalLine: Int,
        contentType: DiffView.ContentType,
        comments: List<Comment>
    ) {
        val model = findThreadModelOnLine(
            providerData, mergeRequestInfo, editor, position, logicalLine, contentType, comments
        )
        model.showEditor = true
        setWritingStateOfGutterIconRenderer(model, logicalLine, contentType)
    }

    protected fun updateComments(
        providerData: ProviderData,
        mergeRequestInfo: MergeRequestInfo,
        editor: EditorEx,
        position: GutterPosition,
        renderer: GutterIconRenderer,
        comments: List<Comment>
    ) {
        val model = findThreadModelOnLine(
            providerData,
            mergeRequestInfo,
            editor,
            position,
            renderer.logicalLine,
            renderer.contentType,
            comments
        )
        model.comments = comments
        updateGutterIcon(renderer, comments)
    }

    protected fun updateGutterIcon(renderer: GutterIconRenderer, comments: List<Comment>) {
        val state = if (comments.isEmpty()) {
            GutterState.NO_COMMENT
        } else {
            if (comments.size == 1) GutterState.THREAD_HAS_SINGLE_COMMENT else GutterState.THREAD_HAS_MULTI_COMMENTS
        }

        renderer.setState(state)
    }

    private fun changeAllCommentsVisibility(displayed: Boolean) {
        val mode = if (displayed) DiffView.DisplayCommentMode.SHOW else DiffView.DisplayCommentMode.HIDE
        for (renderer in myGutterIconRenderersOfBefore.values) {
            dispatcher.multicaster.onGutterActionPerformed(
                renderer, GutterActionType.TOGGLE, mode
            )
        }
        for (renderer in myGutterIconRenderersOfAfter.values) {
            dispatcher.multicaster.onGutterActionPerformed(
                renderer, GutterActionType.TOGGLE, mode
            )
        }
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
        mergeRequestInfo: MergeRequestInfo,
        editor: EditorEx,
        position: GutterPosition,
        logicalLine: Int,
        contentType: DiffView.ContentType,
        comments: List<Comment> = listOf()
    ): ThreadModel {
        val map = if (contentType == DiffView.ContentType.BEFORE) myThreadModelOfBefore else myThreadModelOfAfter
        if (!map.containsKey(logicalLine)) {
            val model = ThreadFactory.makeModel(comments)
            val view = ThreadFactory.makeView(
                applicationService, editor, providerData, mergeRequestInfo, logicalLine, contentType, position
            )
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