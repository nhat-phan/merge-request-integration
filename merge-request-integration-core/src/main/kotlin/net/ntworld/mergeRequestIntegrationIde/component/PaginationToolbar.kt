package net.ntworld.mergeRequestIntegrationIde.component

import net.ntworld.mergeRequestIntegrationIde.Component
import java.util.*

interface PaginationToolbar : Component {
    fun enable()

    fun disable()

    fun getCurrentPage(): Int

    fun setData(page: Int, totalPages: Int, totalItems: Int)

    fun addListener(listener: Listener)

    fun removeListener(listener: Listener)

    interface Listener : EventListener {
        fun changePage(page: Int)
    }
}