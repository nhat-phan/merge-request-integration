package net.ntworld.mergeRequestIntegrationIde.comments.ui

import com.intellij.openapi.Disposable
import com.intellij.util.EventDispatcher
import java.util.*

interface SimpleEventListener : EventListener {

    fun eventOccurred()

    companion object {
        fun addDisposableListener(
                dispatcher: EventDispatcher<SimpleEventListener>,
                disposable: Disposable,
                listener: () -> Unit
        ) {
            dispatcher.addListener(object : SimpleEventListener {
                override fun eventOccurred() {
                    listener()
                }
            }, disposable)
        }
    }
}