package net.ntworld.mergeRequestIntegrationIde.diff

import com.intellij.diff.tools.util.base.DiffViewerBase
import com.intellij.diff.tools.util.base.DiffViewerListener
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.editor.impl.EditorEmbeddedComponentManager
import com.intellij.util.EventDispatcher
import com.intellij.util.ui.JBUI
import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequest.MergeRequest
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequestIntegrationIde.service.ApplicationService
import net.ntworld.mergeRequestIntegrationIde.ui.panel.CommentPanel
import java.awt.Dimension

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
    private val myCommentsGutterOfBefore = mutableMapOf<Int, MutableSet<String>>()
    private val myCommentsGutterOfAfter = mutableMapOf<Int, MutableSet<String>>()

    override val dispatcher = EventDispatcher.create(DiffView.Action::class.java)

    init {
        viewerBase.addListener(diffViewerListener)
    }

    override fun initialize() {
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

    protected fun displayCommentsOnLine(
        providerData: ProviderData,
        mergeRequest: MergeRequest,
        editor: EditorEx,
        logicalLine: Int,
        comments: List<Comment>
    ) {
        val editorEmbeddedComponentManager = EditorEmbeddedComponentManager.getInstance()
        val offset = editor.document.getLineEndOffset(logicalLine)
        val thread = JBUI.Panels.simplePanel()
        for (comment in comments) {
            val panel = CommentPanel(applicationService)
            panel.setComment(providerData, mergeRequest, comment)
            thread.addToBottom(panel.createComponent())
        }
        thread.preferredSize = Dimension(
            editor.scrollPane.viewport.width,
            thread.preferredSize.height
        )
        editorEmbeddedComponentManager.addComponent(
            editor,
            thread,
            EditorEmbeddedComponentManager.Properties(
                EditorEmbeddedComponentManager.ResizePolicy.any(),
                true,
                false,
                0,
                offset
            )
        )
    }

    private fun collectThreadIds(comments: List<Comment>): MutableSet<String> {
        val result = mutableSetOf<String>()
        comments.forEach {
            result.add(it.parentId)
        }
        return result
    }
}