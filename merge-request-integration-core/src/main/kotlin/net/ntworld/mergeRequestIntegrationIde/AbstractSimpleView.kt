package net.ntworld.mergeRequestIntegrationIde

import com.intellij.util.EventDispatcher
import java.util.*

abstract class AbstractSimpleView: SimpleView {
    protected val dispatcher = EventDispatcher.create(EventListener::class.java)

    override fun addActionListener(listener: EventListener) = dispatcher.addListener(listener)

    override fun removeActionListener(listener: EventListener) = dispatcher.removeListener(listener)
}
