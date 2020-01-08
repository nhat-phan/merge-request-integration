package net.ntworld.mergeRequestIntegrationIde.ui.util

import com.intellij.util.EventDispatcher
import net.ntworld.mergeRequestIntegrationIde.ui.Component
import java.util.*

interface PaginationToolbarUI: Component {

    val eventDispatcher: EventDispatcher<PaginationEventListener>

    fun enable()

    fun disable()

    fun getCurrentPage(): Int

    fun setData(page: Int, totalPages: Int, totalItems: Int)

    interface PaginationEventListener : EventListener {
        fun changePage(page: Int)
    }
}