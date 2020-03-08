package net.ntworld.mergeRequestIntegrationIde.ui.util

import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.wm.IdeFocusManager
import com.intellij.ui.awt.RelativePoint
import com.intellij.ui.tabs.*
import com.intellij.ui.tabs.impl.*
import com.intellij.ui.tabs.impl.singleRow.ScrollableSingleRowLayout
import com.intellij.ui.tabs.impl.singleRow.SingleRowLayout
import com.intellij.util.ui.JBUI
import java.awt.*
import javax.swing.JComponent
import com.intellij.openapi.project.Project as IdeaProject

class Tabs(
    private val ideaProject: IdeaProject,
    private val disposable: Disposable
) : TabsUI {
    private val myTabs: JBTabs = MyTabs(ideaProject, disposable)
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

    private class MyTabs(
        private val ideaProject: IdeaProject,
        private val disposable: Disposable
    ) : JBEditorTabs(
        ideaProject,
        ActionManager.getInstance(),
        IdeFocusManager.getInstance(ideaProject),
        disposable
    ) {
        override fun createTabPainterAdapter(): TabPainterAdapter? {
            return DefaultTabPainterAdapter(JBTabPainter.DEBUGGER)
        }

        override fun createSingleRowLayout(): SingleRowLayout? {
            return ScrollableSingleRowLayout(this)
        }

        override fun createTabBorder(): JBTabsBorder? {
            return MyTabsBorder(this)
        }

        override fun useSmallLabels(): Boolean {
            return true
        }

        override fun getToolbarInset(): Int {
            return 0
        }

        fun shouldAddToGlobal(point: Point?): Boolean {
            val label = selectedLabel
            if (label == null || point == null) {
                return true
            }
            val bounds = label.bounds
            return point.y <= bounds.y + bounds.height
        }

        override fun layout(c: JComponent?, bounds: Rectangle): Rectangle? {
            if (c is Toolbar) {
                bounds.height -= separatorWidth
                return super.layout(c, bounds)
            }
            return super.layout(c, bounds)
        }

        override fun processDropOver(over: TabInfo?, relativePoint: RelativePoint) {
            val point = relativePoint.getPoint(component)
            myShowDropLocation = shouldAddToGlobal(point)
            super.processDropOver(over, relativePoint)
            for ((key, label) in myInfo2Label) {
                if (label.bounds.contains(point) && myDropInfo != key) {
                    select(key, false)
                    break
                }
            }
        }

        override fun createTabLabel(info: TabInfo): TabLabel {
            return MyTabLabel(this, info)
        }

        private class MyTabLabel(tabs: JBTabsImpl, info: TabInfo) : TabLabel(tabs, info) {
            override fun getPreferredSize(): Dimension {
                val size = super.getPreferredSize()
                return Dimension(size.width, getPreferredHeight())
            }

            private fun getPreferredHeight(): Int {
                return JBUI.scale(28)
            }
        }

        private class MyTabsBorder(private val jbTabs: JBTabsImpl) : JBTabsBorder(jbTabs) {
            override val effectiveBorder: Insets
                get() = Insets(jbTabs.borderThickness, 0, 0, 0)

            override fun paintBorder(
                c: Component,
                g: Graphics,
                x: Int,
                y: Int,
                width: Int,
                height: Int
            ) {
                if (jbTabs.isEmptyVisible) return
                jbTabs.tabPainter.paintBorderLine(
                    g as Graphics2D, jbTabs.borderThickness, Point(x, y + jbTabs.myHeaderFitSize.height),
                    Point(x + width, y + jbTabs.myHeaderFitSize.height)
                )
            }

        }
    }
}