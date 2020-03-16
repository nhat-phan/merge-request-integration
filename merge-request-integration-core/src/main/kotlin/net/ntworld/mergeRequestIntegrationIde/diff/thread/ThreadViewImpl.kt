package net.ntworld.mergeRequestIntegrationIde.diff.thread

import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.editor.impl.EditorEmbeddedComponentManager
import com.intellij.util.EventDispatcher
import com.intellij.util.ui.JBUI
import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequestIntegrationIde.diff.AddCommentRequestedPosition

class ThreadViewImpl(
    private val editor: EditorEx,
    private val providerData: ProviderData,
    private val logicalLine: Int,
    private val position: AddCommentRequestedPosition
) : ThreadView {
    override val dispatcher = EventDispatcher.create(ThreadView.Action::class.java)

    private val myWrapper = JBUI.Panels.simplePanel()

    init {
        myWrapper.isVisible = true
    }

    override fun initialize() {
        val editorEmbeddedComponentManager = EditorEmbeddedComponentManager.getInstance()
        val offset = editor.document.getLineEndOffset(logicalLine)
        editorEmbeddedComponentManager.addComponent(
            editor,
            myWrapper,
            EditorEmbeddedComponentManager.Properties(
                EditorEmbeddedComponentManager.ResizePolicy.any(),
                true,
                false,
                0,
                offset
            )
        )
    }

    override fun dispose() {
    }

    override fun addCommentPanel(comment: Comment) {
        val commentComponent = makeCommentComponent(providerData, comment)

        myWrapper.addToBottom(commentComponent.createComponent())
    }

    override fun show() {
        myWrapper.isVisible = true
    }

    override fun hide() {
        myWrapper.isVisible = false
    }

    private fun makeCommentComponent(providerData: ProviderData, comment: Comment): CommentComponent {
        return CommentComponentImpl(providerData, comment, 0)
    }
}