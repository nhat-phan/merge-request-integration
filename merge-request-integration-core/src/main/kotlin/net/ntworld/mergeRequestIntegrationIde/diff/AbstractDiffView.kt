package net.ntworld.mergeRequestIntegrationIde.diff

import com.intellij.diff.tools.util.base.DiffViewerBase
import com.intellij.diff.tools.util.base.DiffViewerListener
import com.intellij.diff.util.Side
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
import net.ntworld.mergeRequestIntegrationIde.component.comment.CommentEvent
import net.ntworld.mergeRequestIntegrationIde.component.comment.CommentEventPropagator
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
    private val myGutterIconRenderersOfLeft = mutableMapOf<Int, GutterIconRenderer>()
    private val myGutterIconRenderersOfRight = mutableMapOf<Int, GutterIconRenderer>()

    private val myThreadOfLeft = mutableMapOf<Int, ThreadPresenter>()
    private val myThreadOfRight = mutableMapOf<Int, ThreadPresenter>()
    private val myCommentEventPropagator = CommentEventPropagator(dispatcher)
    private val myThreadPresenterEventListener = object : ThreadPresenter.EventListener,
        CommentEvent by myCommentEventPropagator {
        override fun onMainEditorClosed(threadPresenter: ThreadPresenter) {
            val renderer = findGutterIconRenderer(threadPresenter.view.logicalLine, threadPresenter.view.side)
            if (null !== renderer) {
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
        }

        override fun onReplyCommentRequested(content: String, repliedComment: Comment, logicalLine: Int, side: Side) {
            dispatcher.multicaster.onReplyCommentRequested(content, repliedComment, logicalLine, side)
        }

        override fun onCreateCommentRequested(content: String, position: GutterPosition, logicalLine: Int, side: Side) {
            dispatcher.multicaster.onCreateCommentRequested(content, position, logicalLine, side)
        }
    }

    init {
        viewerBase.addListener(diffViewerListener)
    }

    protected abstract fun convertVisibleLineToLogicalLine(visibleLine: Int, side: Side): Int

    override fun destroyExistingComments(excludedVisibleLines: Set<Int>, side: Side) {
        val excludedLogicalLines = excludedVisibleLines
            .map { convertVisibleLineToLogicalLine(it, side) }
            .filter { it >= 0 }

        val map = if (side == Side.LEFT) myThreadOfLeft else myThreadOfRight
        val removedKeys = mutableListOf<Int>()
        for (entry in map) {
            if (excludedLogicalLines.contains(entry.key)) {
                continue
            }
            entry.value.model.comments = listOf()
            removedKeys.add(entry.key)
        }
        removedKeys.forEach { map.remove(it) }
    }

    override fun showAllComments() = changeAllCommentsVisibility(true)

    override fun hideAllComments() = changeAllCommentsVisibility(false)

    override fun resetGutterIcons() {
        for (renderer in myGutterIconRenderersOfLeft.values) {
            renderer.setState(GutterState.NO_COMMENT)
        }
        for (renderer in myGutterIconRenderersOfRight.values) {
            renderer.setState(GutterState.NO_COMMENT)
        }
    }

    override fun resetEditorOnLine(logicalLine: Int, side: Side, repliedComment: Comment?) {
        val map = if (side == Side.LEFT) myThreadOfLeft else myThreadOfRight

        val thread = map[logicalLine]
        if (null !== thread) {
            thread.model.resetEditor(repliedComment)
        }
    }

    override fun dispose() {
        myGutterIconRenderersOfLeft.clear()
        myGutterIconRenderersOfRight.clear()
    }

    protected fun initializeThreadOnLineIfNotAvailable(
        providerData: ProviderData,
        mergeRequestInfo: MergeRequestInfo,
        editor: EditorEx,
        position: GutterPosition,
        logicalLine: Int,
        side: Side,
        comments: List<Comment>
    ) {
        val map = if (side == Side.LEFT) myThreadOfLeft else myThreadOfRight
        if (!map.containsKey(logicalLine)) {
            val model = ThreadFactory.makeModel(comments)
            val view = ThreadFactory.makeView(
                applicationService, editor, providerData, mergeRequestInfo, logicalLine, side, position
            )
            val presenter = ThreadFactory.makePresenter(model, view)

            presenter.addListener(myThreadPresenterEventListener)
            Disposer.register(this, presenter)

            map[logicalLine] = presenter
        }
    }

    protected fun dispatchOnGutterActionPerformed(renderer: GutterIconRenderer, type: GutterActionType) {
        dispatcher.multicaster.onGutterActionPerformed(renderer, type, DiffView.DisplayCommentMode.TOGGLE)
    }

    protected fun registerGutterIconRenderer(renderer: GutterIconRenderer) {
        val map = if (renderer.side == Side.LEFT) myGutterIconRenderersOfLeft else myGutterIconRenderersOfRight

        map[renderer.logicalLine] = renderer
    }

    protected fun findGutterIconRenderer(logicalLine: Int, side: Side): GutterIconRenderer? {
        val map = if (side == Side.LEFT) myGutterIconRenderersOfLeft else myGutterIconRenderersOfRight

        return map[logicalLine]
    }

    override fun displayComments(visibleLine: Int, side: Side, mode: DiffView.DisplayCommentMode) {
        val logicalLine = convertVisibleLineToLogicalLine(visibleLine, side)
        if (-1 != logicalLine) {
            assertThreadAvailable(logicalLine, side) {
                displayComments(it.model, logicalLine, side, mode)
            }
        }
    }

    override fun displayComments(renderer: GutterIconRenderer, mode: DiffView.DisplayCommentMode) {
        assertThreadAvailable(renderer.logicalLine, renderer.side) {
            displayComments(it.model, renderer.logicalLine, renderer.side, mode)
        }
    }

    protected fun displayComments(
        model: ThreadModel,
        logicalLine: Int,
        side: Side,
        mode: DiffView.DisplayCommentMode
    ) {
        when (mode) {
            DiffView.DisplayCommentMode.TOGGLE -> model.visible = !model.visible
            DiffView.DisplayCommentMode.SHOW -> model.visible = true
            DiffView.DisplayCommentMode.HIDE -> model.visible = false
        }
        setWritingStateOfGutterIconRenderer(model, logicalLine, side)
    }

    override fun displayEditorOnLine(logicalLine: Int, side: Side) {
        assertThreadAvailable(logicalLine, side) {
            it.model.showEditor = true
            setWritingStateOfGutterIconRenderer(it.model, logicalLine, side)
        }
    }

    protected fun updateComments(renderer: GutterIconRenderer, comments: List<Comment>) {
        assertThreadAvailable(renderer.logicalLine, renderer.side) {
            it.model.comments = comments
        }
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
        for (renderer in myGutterIconRenderersOfLeft.values) {
            dispatcher.multicaster.onGutterActionPerformed(
                renderer, GutterActionType.TOGGLE, mode
            )
        }
        for (renderer in myGutterIconRenderersOfRight.values) {
            dispatcher.multicaster.onGutterActionPerformed(
                renderer, GutterActionType.TOGGLE, mode
            )
        }
    }

    private fun setWritingStateOfGutterIconRenderer(model: ThreadModel, logicalLine: Int, side: Side) {
        val renderer = findGutterIconRenderer(logicalLine, side)
        if (null !== renderer && model.showEditor) {
            renderer.setState(GutterState.WRITING)
        }
    }

    private fun assertThreadAvailable(logicalLine: Int, side: Side, invoker: ((ThreadPresenter) -> Unit)) {
        val map = if (side == Side.LEFT) myThreadOfLeft else myThreadOfRight
        val thread = map[logicalLine]
        if (null !== thread) {
            invoker.invoke(thread)
        }
    }

    protected fun findThreadPresenter(logicalLine: Int, side: Side): ThreadPresenter? {
        val map = if (side == Side.LEFT) myThreadOfLeft else myThreadOfRight
        return map[logicalLine]
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