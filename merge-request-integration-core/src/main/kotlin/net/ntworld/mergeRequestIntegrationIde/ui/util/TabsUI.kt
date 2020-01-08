package net.ntworld.mergeRequestIntegrationIde.ui.util

import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.ui.tabs.JBTabs
import com.intellij.ui.tabs.TabInfo
import com.intellij.ui.tabs.TabsListener
import javax.swing.JComponent

interface TabsUI {
    val component
        get() = getTabs().component

    fun getTabs(): JBTabs

    fun setCommonCenterActionGroupFactory(factory: () -> ActionGroup)

    fun setCommonSideComponentFactory(factory: () -> JComponent)

    fun addTab(tabInfo: TabInfo)

    fun addListener(listener: TabsListener)
}