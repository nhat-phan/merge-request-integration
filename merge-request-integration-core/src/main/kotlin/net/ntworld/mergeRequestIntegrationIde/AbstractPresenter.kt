package net.ntworld.mergeRequestIntegrationIde

import com.intellij.util.EventDispatcher
import java.util.*

abstract class AbstractPresenter<T: EventListener> : Presenter<T> {
    protected abstract val dispatcher: EventDispatcher<T>

    override fun addListener(listener: T) = dispatcher.addListener(listener)

    override fun removeListener(listener: T) = dispatcher.removeListener(listener)
}
