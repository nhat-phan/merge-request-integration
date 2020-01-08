package net.ntworld.mergeRequestIntegrationIde.ui.util

import com.intellij.execution.ui.layout.impl.JBRunnerTabs
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.ui.tabs.JBTabs
import com.intellij.ui.tabs.TabInfo
import com.intellij.ui.tabs.TabsListener
import javax.swing.JComponent
import com.intellij.openapi.project.Project as IdeaProject

class Tabs(
    private val ideaProject: IdeaProject,
    private val disposable: Disposable
) : TabsUI {
    private val myTabs = JBRunnerTabs.create(ideaProject, disposable)
    private var myCommonActionGroupInCenterFactory: (() -> ActionGroup)? = null
    private var myCommonSideComponentFactory: (() -> JComponent)? = null

    override fun getTabs(): JBTabs = myTabs

    override fun setCommonCenterActionGroupFactory(factory: () -> ActionGroup) {
        myCommonActionGroupInCenterFactory = factory
    }

    override fun setCommonSideComponentFactory(factory: () -> JComponent) {
        myCommonSideComponentFactory = factory
    }

    override fun addTab(tabInfo: TabInfo) {
        if (null !== myCommonActionGroupInCenterFactory) {
            tabInfo.setActions(myCommonActionGroupInCenterFactory!!.invoke(), null)
        }
        if (null !== myCommonSideComponentFactory) {
            tabInfo.sideComponent = myCommonSideComponentFactory!!.invoke()
        }
        myTabs.addTab(tabInfo)
    }

    override fun addListener(listener: TabsListener) {
        myTabs.addListener(listener)
    }
}