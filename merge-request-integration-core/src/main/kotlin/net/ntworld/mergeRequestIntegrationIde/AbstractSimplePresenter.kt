package net.ntworld.mergeRequestIntegrationIde

import com.intellij.util.EventDispatcher
import java.util.*

abstract class AbstractSimplePresenter : SimplePresenter {
    protected val dispatcher = EventDispatcher.create(EventListener::class.java)

    override fun addListener(listener: EventListener) = dispatcher.addListener(listener)

    override fun removeListener(listener: EventListener) = dispatcher.removeListener(listener)
}
