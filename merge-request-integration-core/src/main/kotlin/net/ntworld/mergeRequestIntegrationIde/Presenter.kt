package net.ntworld.mergeRequestIntegrationIde

import com.intellij.util.EventDispatcher
import java.util.*

interface Presenter<Event : EventListener> {
    val dispatcher: EventDispatcher<Event>
}