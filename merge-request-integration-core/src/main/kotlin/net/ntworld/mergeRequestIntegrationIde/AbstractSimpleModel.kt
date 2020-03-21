package net.ntworld.mergeRequestIntegrationIde

import com.intellij.util.EventDispatcher
import java.util.*

abstract class AbstractSimpleModel: SimpleModel {
    protected val dispatcher = EventDispatcher.create(EventListener::class.java)

    override fun addDataListener(listener: EventListener) = dispatcher.addListener(listener)

    override fun removeDataListener(listener: EventListener) = dispatcher.removeListener(listener)
}
