package net.ntworld.mergeRequestIntegrationIde

import com.intellij.util.EventDispatcher
import java.util.*

abstract class AbstractModel<DataListener: EventListener>: Model<DataListener> {
    protected abstract val dispatcher: EventDispatcher<DataListener>

    override fun addDataListener(listener: DataListener) = dispatcher.addListener(listener)

    override fun removeDataListener(listener: DataListener) = dispatcher.removeListener(listener)
}
