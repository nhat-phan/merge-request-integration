package net.ntworld.mergeRequestIntegrationIde

import java.util.*

interface Presenter<T : EventListener> {
    fun addListener(listener: T)

    fun removeListener(listener: T)
}