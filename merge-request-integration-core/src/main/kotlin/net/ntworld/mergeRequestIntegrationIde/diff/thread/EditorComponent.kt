package net.ntworld.mergeRequestIntegrationIde.diff.thread

import com.intellij.openapi.Disposable
import com.intellij.util.EventDispatcher
import net.ntworld.mergeRequestIntegrationIde.Component
import java.util.*

interface EditorComponent : Component, Disposable {
    val dispatcher: EventDispatcher<Event>

    var isVisible: Boolean

    fun focus()

    interface Event: EventListener {
        fun onEditorResized()
    }

    enum class Type {
        NEW_DISCUSSION,
        REPLY
    }
}
