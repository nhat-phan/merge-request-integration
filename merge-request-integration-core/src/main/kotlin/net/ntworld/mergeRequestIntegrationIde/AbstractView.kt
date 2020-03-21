package net.ntworld.mergeRequestIntegrationIde

import com.intellij.util.EventDispatcher
import java.util.*

abstract class AbstractView<ActionListener: EventListener>: View<ActionListener> {
    protected abstract val dispatcher: EventDispatcher<ActionListener>

    override fun addActionListener(listener: ActionListener) = dispatcher.addListener(listener)

    override fun removeActionListener(listener: ActionListener) = dispatcher.removeListener(listener)
}
