package net.ntworld.mergeRequestIntegrationIde.component.comment

import com.intellij.openapi.Disposable
import net.ntworld.mergeRequestIntegrationIde.Component

interface EditorComponent : Component, Disposable {
    var text: String

    var addCommentNowButtonText: String

    var addCommentNowButtonDesc: String

    var startAReviewButtonText: String

    var startAReviewButtonDesc: String

    var isVisible: Boolean

    fun focus()

    fun drawBorderTop(display: Boolean)

    fun addListener(listener: EventListener)

    interface EventListener: java.util.EventListener {
        fun onEditorFocused(editor: EditorComponent)

        fun onEditorResized(editor: EditorComponent)

        fun onCancelClicked(editor: EditorComponent)

        fun onSubmitClicked(editor: EditorComponent, isDraft: Boolean)
    }

    enum class Type {
        NEW_DISCUSSION,
        EDIT,
        REPLY
    }
}
