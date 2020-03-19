package net.ntworld.mergeRequestIntegrationIde.diff.thread

import com.intellij.openapi.Disposable
import com.intellij.util.EventDispatcher
import net.ntworld.mergeRequestIntegrationIde.ui.Component
import java.util.*

interface EditorComponent : Component, Disposable {
    val dispatcher: EventDispatcher<Event>

    var isVisible: Boolean

    interface Event: EventListener {
        fun onEditorResized()
    }
}
