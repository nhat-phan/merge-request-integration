package net.ntworld.mergeRequestIntegrationIde

import com.intellij.util.EventDispatcher
import java.util.*

interface Model<Change : EventListener> {
    val dispatcher: EventDispatcher<Change>
}