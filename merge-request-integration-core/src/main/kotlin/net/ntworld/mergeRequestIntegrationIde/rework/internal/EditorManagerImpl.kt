package net.ntworld.mergeRequestIntegrationIde.rework.internal

import com.intellij.diff.util.Side
import com.intellij.notification.NotificationType
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.editor.markup.HighlighterLayer
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vcs.changes.Change
import com.intellij.openapi.vfs.VirtualFile
import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequest.command.DeleteCommentCommand
import net.ntworld.mergeRequest.command.ResolveCommentCommand
import net.ntworld.mergeRequest.command.UnresolveCommentCommand
import net.ntworld.mergeRequest.request.ReplyCommentRequest
import net.ntworld.mergeRequestIntegration.make
import net.ntworld.mergeRequestIntegration.provider.ProviderException
import net.ntworld.mergeRequestIntegrationIde.component.gutter.*
import net.ntworld.mergeRequestIntegrationIde.diff.DiffView
import net.ntworld.mergeRequestIntegrationIde.component.thread.ThreadFactory
import net.ntworld.mergeRequestIntegrationIde.component.thread.ThreadModel
import net.ntworld.mergeRequestIntegrationIde.component.thread.ThreadPresenter
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
    private val myGutterIconRenderers = Collections.synchronizedMap(
        mutableMapOf<TextEditor, MutableMap<Int, GutterIconRenderer>>()
    )
    private val myThreads = Collections.synchronizedMap(
        mutableMapOf<TextEditor, MutableMap<Int, ThreadPresenter>>()
    )

    override fun initialize(textEditor: TextEditor, reworkWatcher: ReworkWatcher) {
        if (myInitializedEditors.containsKey(textEditor)) {
            return
        }
        val virtualFile = textEditor.file ?: return
        val change = reworkWatcher.findChangeByPath(virtualFile.path) ?: return

        registerGutterIcons(textEditor, reworkWatcher, virtualFile, change)

        val commentsByLines = CommentUtil.groupCommentsByPositionNewLine(
            reworkWatcher.findCommentsByPath(virtualFile.path)
        )
        commentsByLines.forEach { (visibleLine, comments) ->
            initializeLine(textEditor, reworkWatcher, visibleLine, change, comments)
        }

        myInitializedEditors[textEditor] = reworkWatcher
    }

    override fun updateComments(textEditor: TextEditor, reworkWatcher: ReworkWatcher) {
        if (!myInitializedEditors.containsKey(textEditor)) {
            return initialize(textEditor, reworkWatcher)
        }

        resetGutterIcons(textEditor)
        val virtualFile = textEditor.file ?: return
        val change = reworkWatcher.findChangeByPath(virtualFile.path) ?: return
        val commentsByLines = CommentUtil.groupCommentsByPositionNewLine(
            reworkWatcher.findCommentsByPath(virtualFile.path)
        )
        commentsByLines.forEach { (visibleLine, comments) ->
            initializeLine(textEditor, reworkWatcher, visibleLine, change, comments)
            assertThreadAvailable(textEditor, visibleLine - 1) {
                it.model.comments = comments
            }
        }
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
        myGutterIconRenderers.remove(textEditor)
        myThreads.remove(textEditor)
    }

    private fun resetGutterIcons(textEditor: TextEditor) {
        val renderers = myGutterIconRenderers[textEditor]
        if (null !== renderers) {
            for (renderer in renderers.values) {
                renderer.setState(GutterState.NO_COMMENT)
            }
        }
    }

    private fun initializeLine(
        textEditor: TextEditor,
        reworkWatcher: ReworkWatcher,
        visibleLine: Int,
        change: Change,
        comments: List<Comment>
    ) {
        val logicalLine = visibleLine - 1
        initializeThreadOnLineIfNotAvailable(textEditor, reworkWatcher, logicalLine, change, comments)
        val renderer = findGutterIconRenderer(textEditor, logicalLine)
        if (null !== renderer) {
            updateGutterIcon(renderer, comments)
        }
    }

    private fun findGutterIconRenderer(textEditor: TextEditor, logicalLine: Int): GutterIconRenderer? {
        val map = myGutterIconRenderers[textEditor] ?: return null

        return map[logicalLine]
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

    private fun initializeThreadOnLineIfNotAvailable(
        textEditor: TextEditor,
        reworkWatcher: ReworkWatcher,
        logicalLine: Int,
        change: Change,
        comments: List<Comment>
    ) {
        val editor = textEditor.editor as? EditorEx ?: return

        if (!myThreads.containsKey(textEditor)) {
            myThreads[textEditor] = mutableMapOf()
        }
        val map = myThreads[textEditor] ?: return
        if (!map.containsKey(logicalLine)) {
            val model = ThreadFactory.makeModel(comments)
            val view = ThreadFactory.makeView(
                projectServiceProvider, editor, reworkWatcher.providerData,
                reworkWatcher.mergeRequestInfo, logicalLine, Side.RIGHT,
                GutterPosition(
                    editorType = DiffView.EditorType.SINGLE_SIDE,
                    changeType = DiffView.ChangeType.UNKNOWN,
                    newLine = logicalLine + 1,
                    newPath = change.afterRevision!!.file.toString(),
                    oldLine = null,
                    oldPath = null,
                    headHash = change.afterRevision!!.revisionNumber.asString()
                )
            )
            val presenter = ThreadFactory.makePresenter(model, view)

            presenter.addListener(MyThreadPresenterEventListener(this, textEditor, reworkWatcher))
            Disposer.register(textEditor, presenter)

            map[logicalLine] = presenter
        }
    }

    private fun registerGutterIcons(
        textEditor: TextEditor,
        reworkWatcher: ReworkWatcher,
        virtualFile: VirtualFile,
        change: Change
    ) {
        if (!myGutterIconRenderers.containsKey(textEditor)) {
            myGutterIconRenderers[textEditor] = mutableMapOf()
        }
        val gutterIconsMap: MutableMap<Int, GutterIconRenderer> = myGutterIconRenderers[textEditor] ?: return

        val editor = textEditor.editor
        val lineCount = editor.document.lineCount
        for (logicalLine in 0 until lineCount) {
            gutterIconsMap[logicalLine] = GutterIconRendererFactory.makeGutterIconRenderer(
                editor.markupModel.addLineHighlighter(logicalLine, HighlighterLayer.LAST, null),
                shouldDisplayAddIcon,
                logicalLine,
                visibleLineLeft = null,
                visibleLineRight = logicalLine + 1,
                side = Side.RIGHT,
                actionListener = MyGutterIconRendererActionListener(
                    this, textEditor, reworkWatcher, virtualFile, change
                )
            )
        }
    }

    private fun displayComment(textEditor: TextEditor, logicalLine: Int) {
        this.assertThreadAvailable(textEditor, logicalLine) {
            this.displayComments(textEditor, it.model, logicalLine, DiffView.DisplayCommentMode.TOGGLE)
        }
    }

    private fun assertThreadAvailable(textEditor: TextEditor, logicalLine: Int, invoker: ((ThreadPresenter) -> Unit)) {
        val map = myThreads[textEditor] ?: return
        val thread = map[logicalLine] ?: return

        invoker.invoke(thread)
    }

    private fun displayComments(
        textEditor: TextEditor,
        model: ThreadModel,
        logicalLine: Int,
        mode: DiffView.DisplayCommentMode
    ) {
        when (mode) {
            DiffView.DisplayCommentMode.TOGGLE -> model.visible = !model.visible
            DiffView.DisplayCommentMode.SHOW -> model.visible = true
            DiffView.DisplayCommentMode.HIDE -> model.visible = false
        }
        setWritingStateOfGutterIconRenderer(textEditor, model, logicalLine)
    }

    private fun setWritingStateOfGutterIconRenderer(textEditor: TextEditor, model: ThreadModel, logicalLine: Int) {
        val renderer = findGutterIconRenderer(textEditor, logicalLine)
        if (null !== renderer && model.showEditor) {
            renderer.setState(GutterState.WRITING)
        }
    }

    private fun resetEditorOnLine(textEditor: TextEditor, logicalLine: Int, repliedComment: Comment) {
        val map = myThreads[textEditor] ?: return
        val thread = map[logicalLine] ?: return
        thread.model.resetEditor(repliedComment)
    }

    private class MyGutterIconRendererActionListener(
        private val self: EditorManagerImpl,
        private val textEditor: TextEditor,
        private val reworkWatcher: ReworkWatcher,
        private val virtualFile: VirtualFile,
        private val change: Change
    ) : GutterIconRendererActionListener {
        override fun performGutterIconRendererAction(gutterIconRenderer: GutterIconRenderer, type: GutterActionType) {
            when (type) {
                GutterActionType.ADD -> {
                }
                GutterActionType.TOGGLE -> {
                    self.initializeThreadOnLineIfNotAvailable(
                        textEditor, reworkWatcher, gutterIconRenderer.logicalLine, change,
                        collectCommentsOfLine(gutterIconRenderer.logicalLine)
                    )
                    self.displayComment(textEditor, gutterIconRenderer.logicalLine)
                }
            }
        }

        private fun collectCommentsOfLine(logicalLine: Int): List<Comment> {
            val visibleLine = logicalLine + 1
            val comments = reworkWatcher.findCommentsByPath(virtualFile.path)
            return comments.filter {
                val position = it.position ?: return@filter false

                position.newLine == visibleLine
            }
        }
    }

    private class MyThreadPresenterEventListener(
        private val self: EditorManagerImpl,
        private val textEditor: TextEditor,
        private val reworkWatcher: ReworkWatcher
    ) : ThreadPresenter.EventListener {
        override fun onMainEditorClosed(threadPresenter: ThreadPresenter) {
        }

        override fun onReplyCommentRequested(content: String, repliedComment: Comment, logicalLine: Int, side: Side) {
            self.projectServiceProvider.infrastructure.serviceBus() process ReplyCommentRequest.make(
                providerId = reworkWatcher.providerData.id,
                mergeRequestId = reworkWatcher.mergeRequestInfo.id,
                repliedComment = repliedComment,
                body = content
            ) ifError {
                self.projectServiceProvider.notify(
                    "There was an error from server. \n\n ${it.message}",
                    NotificationType.ERROR
                )
                throw ProviderException(it)
            }
            reworkWatcher.fetchComments()
            self.resetEditorOnLine(textEditor, logicalLine, repliedComment)
        }

        override fun onCreateCommentRequested(content: String, position: GutterPosition, logicalLine: Int, side: Side) {
        }

        override fun onDeleteCommentRequested(comment: Comment) {
            self.projectServiceProvider.infrastructure.commandBus() process DeleteCommentCommand.make(
                providerId = reworkWatcher.providerData.id,
                mergeRequestId = reworkWatcher.mergeRequestInfo.id,
                comment = comment
            )
            reworkWatcher.fetchComments()
        }

        override fun onResolveCommentRequested(comment: Comment) {
            self.projectServiceProvider.infrastructure.commandBus() process ResolveCommentCommand.make(
                providerId = reworkWatcher.providerData.id,
                mergeRequestId = reworkWatcher.mergeRequestInfo.id,
                comment = comment
            )
            reworkWatcher.fetchComments()
        }

        override fun onUnresolveCommentRequested(comment: Comment) {
            self.projectServiceProvider.infrastructure.commandBus() process UnresolveCommentCommand.make(
                providerId = reworkWatcher.providerData.id,
                mergeRequestId = reworkWatcher.mergeRequestInfo.id,
                comment = comment
            )
            reworkWatcher.fetchComments()
        }
    }
}