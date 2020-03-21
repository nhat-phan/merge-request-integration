package net.ntworld.mergeRequestIntegrationIde

import java.util.*

interface View<ActionListener : EventListener> {

    fun addActionListener(listener: ActionListener)

    fun removeActionListener(listener: ActionListener)

}