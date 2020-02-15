package net.ntworld.mergeRequestIntegrationIde.comments.ui

import com.intellij.openapi.Disposable
import com.intellij.util.EventDispatcher
import org.jetbrains.annotations.CalledInAwt
import kotlin.properties.Delegates

class SingleValueModel<T>(initialValue: T) {

    private val changeEventDispatcher = EventDispatcher.create(SimpleEventListener::class.java)

    var value by Delegates.observable<T>(initialValue) { _, _, _ ->
        changeEventDispatcher.multicaster.eventOccurred()
    }

    @CalledInAwt
    fun addValueChangedListener(disposable: Disposable, listener: () -> Unit) =
            changeEventDispatcher.addListener(object : SimpleEventListener {
                override fun eventOccurred() {
                    listener()
                }
            }, disposable)

    @CalledInAwt
    fun addValueChangedListener(listener: () -> Unit) =
            changeEventDispatcher.addListener(object : SimpleEventListener {
                override fun eventOccurred() {
                    listener()
                }
            })
}