package net.ntworld.mergeRequestIntegrationIde.rework.internal

import com.intellij.diff.util.Side
import com.intellij.openapi.editor.LogicalPosition
import com.intellij.openapi.editor.ScrollType
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.editor.markup.HighlighterLayer
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.util.Disposer
import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequest.MergeRequestInfo
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequestIntegrationIde.component.gutter.*
import net.ntworld.mergeRequestIntegrationIde.component.thread.ThreadFactory
import net.ntworld.mergeRequestIntegrationIde.component.thread.ThreadModel
import net.ntworld.mergeRequestIntegrationIde.component.thread.ThreadPresenter
import net.ntworld.mergeRequestIntegrationIde.diff.DiffView
import net.ntworld.mergeRequestIntegrationIde.infrastructure.ProjectServiceProvider
import net.ntworld.mergeRequestIntegrationIde.infrastructure.notifier.ReworkWatcherNotifier
import net.ntworld.mergeRequestIntegrationIde.rework.ReworkEditorController
import net.ntworld.mergeRequestIntegrationIde.util.CommentUtil

class ReworkEditorControllerImpl(
    private val projectServiceProvider: ProjectServiceProvider,
    override val textEditor: TextEditor,
    override val editor: EditorEx,
    override val providerData: ProviderData,
    override val mergeRequestInfo: MergeRequestInfo,
    override val path: String,
    override val revisionNumber: String
) : ReworkEditorController {
    private val shouldDisplayAddIcon = false
    private val myReworkRequester = projectServiceProvider.messageBus.syncPublisher(ReworkWatcherNotifier.TOPIC)

    private val myGutterIconRenderers = mutableMapOf<Int, GutterIconRenderer>()
    private val myThreadPresenters = mutableMapOf<Int, ThreadPresenter>()

    init {
        registerGutterIconRenderers()

        val reworkWatcher = projectServiceProvider.reworkManager.findActiveReworkWatcher(providerData)
        if (null !== reworkWatcher) {
            val commentsByLines = CommentUtil.groupCommentsByPositionNewLine(
                reworkWatcher.findCommentsByPath(path)
            )
            commentsByLines.forEach { (visibleLine, comments) ->
                initializeLine(visibleLine, comments)
            }
        }

        Disposer.register(textEditor, this)
    }

    override fun dispose() {
        val makeupModel = textEditor.editor.markupModel
        val highlighters = makeupModel.allHighlighters
        for (highlighter in highlighters) {
            if (highlighter.gutterIconRenderer is GutterIconRenderer) {
                makeupModel.removeHighlighter(highlighter)
                highlighter.dispose()
            }
        }

        myGutterIconRenderers.clear()
        myThreadPresenters.forEach { it.value.dispose() }
        myThreadPresenters.clear()
    }

    override fun updateComments() {
        resetGutterIcons()

        val reworkWatcher = projectServiceProvider.reworkManager.findActiveReworkWatcher(providerData)
        if (null !== reworkWatcher) {
            val commentsByLines = CommentUtil.groupCommentsByPositionNewLine(
                reworkWatcher.findCommentsByPath(path)
            )
            destroyExistingComments(commentsByLines.keys)
            commentsByLines.forEach { (visibleLine, comments) ->
                initializeLine(visibleLine, comments)
                assertThreadPresenterAvailable(visibleLine - 1) {
                    it.model.comments = comments
                }
            }
        }
    }

    override fun hideAllComments() {
        for (presenter in myThreadPresenters) {
            presenter.value.model.visible = false
        }
    }

    override fun displayCommentsOnLine(visibleLine: Int) {
        this.assertThreadPresenterAvailable(visibleLine - 1) {
            this.displayComments(it.model, visibleLine - 1, DiffView.DisplayCommentMode.SHOW)
        }
    }

    override fun scrollToLine(visibleLine: Int) {
        editor.scrollingModel.scrollTo(LogicalPosition(visibleLine - 1, 0), ScrollType.MAKE_VISIBLE)
    }

    private fun initializeLine(visibleLine: Int, comments: List<Comment>) {
        val logicalLine = visibleLine - 1
        initializeThreadPresenterOnLineIfNotAvailable(logicalLine, comments)
        val renderer = myGutterIconRenderers[logicalLine]
        if (null !== renderer) {
            updateGutterIcon(renderer, comments)
        }
    }

    private fun resetGutterIcons() {
        for (renderer in myGutterIconRenderers.values) {
            renderer.setState(GutterState.NO_COMMENT)
        }
    }

    private fun destroyExistingComments(excludedVisibleLines: Set<Int>) {
        val excludedLogicalLines = excludedVisibleLines
            .map { it - 1 }
            .filter { it >= 0 }

        val removedKeys = mutableListOf<Int>()
        for (entry in myThreadPresenters) {
            if (excludedLogicalLines.contains(entry.key)) {
                continue
            }
            entry.value.model.comments = listOf()
            removedKeys.add(entry.key)
        }
        removedKeys.forEach { myThreadPresenters.remove(it) }
    }

    private fun resetEditorOnLine(logicalLine: Int, repliedComment: Comment?) {
        val thread = myThreadPresenters[logicalLine] ?: return
        thread.model.resetEditor(repliedComment)
    }

    // TODO: Duplicate AbstractDiffView.updateGutterIcon maybe move to Util class
    private fun updateGutterIcon(renderer: GutterIconRenderer, comments: List<Comment>) {
        val state = if (comments.isEmpty()) {
            GutterState.NO_COMMENT
        } else {
            if (comments.size == 1) GutterState.THREAD_HAS_SINGLE_COMMENT else GutterState.THREAD_HAS_MULTI_COMMENTS
        }

        renderer.setState(state)
    }

    private fun registerGutterIconRenderers() {
        val lineCount = editor.document.lineCount
        for (logicalLine in 0 until lineCount) {
            myGutterIconRenderers[logicalLine] = GutterIconRendererFactory.makeGutterIconRenderer(
                editor.markupModel.addLineHighlighter(logicalLine, HighlighterLayer.LAST, null),
                shouldDisplayAddIcon,
                logicalLine,
                visibleLineLeft = null,
                visibleLineRight = logicalLine + 1,
                side = Side.RIGHT,
                actionListener = MyGutterIconRendererActionListener(this)
            )
        }
    }

    private fun initializeThreadPresenterOnLineIfNotAvailable(logicalLine: Int, comments: List<Comment>) {
        if (!myThreadPresenters.containsKey(logicalLine)) {
            val model = ThreadFactory.makeModel(comments)
            val view = ThreadFactory.makeView(
                projectServiceProvider, editor, providerData, mergeRequestInfo, logicalLine, Side.RIGHT,
                GutterPosition(
                    editorType = DiffView.EditorType.SINGLE_SIDE,
                    changeType = DiffView.ChangeType.UNKNOWN,
                    newLine = logicalLine + 1,
                    newPath = path,
                    oldLine = null,
                    oldPath = null,
                    headHash = revisionNumber
                ),
                replyInDialog = true
            )
            val presenter = ThreadFactory.makePresenter(model, view)

            presenter.addListener(MyThreadPresenterEventListener(this))
            Disposer.register(textEditor, presenter)

            myThreadPresenters[logicalLine] = presenter
        }
    }

    private fun assertThreadPresenterAvailable(logicalLine: Int, invoker: ((ThreadPresenter) -> Unit)) {
        val thread = myThreadPresenters[logicalLine] ?: return

        invoker.invoke(thread)
    }

    private fun toggleComments(logicalLine: Int) {
        this.assertThreadPresenterAvailable(logicalLine) {
            this.displayComments(it.model, logicalLine, DiffView.DisplayCommentMode.TOGGLE)
        }
    }

    private fun displayComments(model: ThreadModel, logicalLine: Int, mode: DiffView.DisplayCommentMode) {
        when (mode) {
            DiffView.DisplayCommentMode.TOGGLE -> model.visible = !model.visible
            DiffView.DisplayCommentMode.SHOW -> model.visible = true
            DiffView.DisplayCommentMode.HIDE -> model.visible = false
        }
        setWritingStateOfGutterIconRenderer(model, logicalLine)
    }

    private fun setWritingStateOfGutterIconRenderer(model: ThreadModel, logicalLine: Int) {
        val renderer = myGutterIconRenderers[logicalLine]
        if (null !== renderer && model.showEditor) {
            renderer.setState(GutterState.WRITING)
        }
    }

    private fun collectCommentsOfLine(logicalLine: Int): List<Comment> {
        val visibleLine = logicalLine + 1
        val reworkWatcher = projectServiceProvider.reworkManager.findActiveReworkWatcher(providerData)
        if (null !== reworkWatcher) {
            val comments = reworkWatcher.findCommentsByPath(path)
            return comments.filter {
                val position = it.position ?: return@filter false

                position.newLine == visibleLine
            }
        }
        return listOf()
    }

    private class MyGutterIconRendererActionListener(
        private val self: ReworkEditorControllerImpl
    ) : GutterIconRendererActionListener {

        override fun performGutterIconRendererAction(gutterIconRenderer: GutterIconRenderer, type: GutterActionType) {
            when (type) {
                GutterActionType.ADD -> {
                }
                GutterActionType.TOGGLE -> {
                    self.initializeThreadPresenterOnLineIfNotAvailable(
                        gutterIconRenderer.logicalLine, self.collectCommentsOfLine(gutterIconRenderer.logicalLine)
                    )
                    self.toggleComments(gutterIconRenderer.logicalLine)
                }
            }
        }

    }

    private class MyThreadPresenterEventListener(
        private val self: ReworkEditorControllerImpl
    ) : ThreadPresenter.EventListener {
        override fun onMainEditorClosed(threadPresenter: ThreadPresenter) {
        }

        override fun onEditCommentRequested(comment: Comment, content: String) {
            self.myReworkRequester.requestEditComment(self.providerData, comment, content)
        }

        override fun onReplyCommentRequested(content: String, repliedComment: Comment, logicalLine: Int, side: Side) {
            self.myReworkRequester.requestReplyComment(self.providerData, content, repliedComment)
            self.resetEditorOnLine(logicalLine, repliedComment)
        }

        override fun onCreateCommentRequested(content: String, position: GutterPosition, logicalLine: Int, side: Side, isDraft: Boolean) {
            self.myReworkRequester.requestCreateComment(self.providerData, content, position, isDraft)
            self.resetEditorOnLine(logicalLine, null)
        }

        override fun onPublishDraftCommentRequested(comment: Comment) {
            self.myReworkRequester.requestPublishComment(self.providerData, comment)
        }

        override fun onDeleteCommentRequested(comment: Comment) {
            self.myReworkRequester.requestDeleteComment(self.providerData, comment)
        }

        override fun onResolveCommentRequested(comment: Comment) {
            self.myReworkRequester.requestResolveComment(self.providerData, comment)
        }

        override fun onUnresolveCommentRequested(comment: Comment) {
            self.myReworkRequester.requestUnresolveComment(self.providerData, comment)
        }
    }
}