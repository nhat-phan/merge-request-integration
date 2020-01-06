package net.ntworld.mergeRequestIntegrationIde.ui.util

import com.intellij.openapi.actionSystem.ActionToolbar
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.DataProvider
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBPanelWithEmptyText
import com.intellij.ui.switcher.QuickActionProvider
import com.intellij.util.ui.UIUtil
import org.jetbrains.annotations.NonNls
import java.awt.BorderLayout
import java.awt.Container
import java.awt.Graphics
import java.awt.event.ContainerAdapter
import java.awt.event.ContainerEvent
import javax.swing.JComponent
import javax.swing.SwingConstants

class CustomSimpleToolWindowPanel(
    private val vertical: Boolean,
    private val borderless: Boolean
) : JBPanelWithEmptyText(), QuickActionProvider, DataProvider {
    private var myToolbar: JComponent? = null
    private var myContent: JComponent? = null

    private var myBorderless = borderless
    private var myVertical = vertical
    private var myProvideQuickActions = false

    init {
        layout = BorderLayout(if (vertical) 0 else 1, if (vertical) 1 else 0)
        setProvideQuickActions(true)

        addContainerListener(object : ContainerAdapter() {
            override fun componentAdded(e: ContainerEvent?) {
                val child = e!!.child

                if (child is Container) {
                    child.addContainerListener(this)
                }
                if (myBorderless) {
                    UIUtil.removeScrollBorder(this@CustomSimpleToolWindowPanel)
                }
            }

            override fun componentRemoved(e: ContainerEvent?) {
                val child = e!!.child

                if (child is Container) {
                    child.removeContainerListener(this)
                }
            }
        })
    }

    fun isVertical(): Boolean {
        return myVertical
    }

    fun setVertical(vertical: Boolean) {
        if (myVertical == vertical) return
        removeAll()
        myVertical = vertical
        setContent(myContent)
        toolbar = myToolbar
    }

    fun isToolbarVisible(): Boolean {
        return myToolbar != null && myToolbar!!.isVisible
    }

    var toolbar: JComponent?
        get() {
            return myToolbar
        }
        set(c) {
            if (c == null) {
                remove(myToolbar)
            }
            myToolbar = c
            if (myToolbar is ActionToolbar) {
                (myToolbar as ActionToolbar).setOrientation(if (myVertical) SwingConstants.HORIZONTAL else SwingConstants.VERTICAL)
            }
            if (c != null) {
                if (myVertical) {
                    add(c, BorderLayout.SOUTH)
                } else {
                    add(c, BorderLayout.EAST)
                }
            }
            revalidate()
            repaint()
        }

    override fun getData(@NonNls dataId: String): Any? {
        return if (QuickActionProvider.KEY.`is`(dataId) && myProvideQuickActions) this else null
    }

    fun setProvideQuickActions(provide: Boolean): CustomSimpleToolWindowPanel? {
        myProvideQuickActions = provide
        return this
    }

    override fun getActions(originalProvider: Boolean): List<AnAction> {
        val toolbars = UIUtil.uiTraverser(myToolbar).traverse().filter(
            ActionToolbar::class.java
        )
        return if (toolbars.size() == 0) emptyList() else toolbars.flatten { toolbar: ActionToolbar -> toolbar.actions }.toList()
    }

    override fun getComponent(): JComponent? {
        return this
    }

    fun getContent(): JComponent? {
        return myContent
    }


    fun setContent(c: JComponent?) {
        if (myContent != null) {
            remove(myContent)
        }
        myContent = c
        add(c, BorderLayout.CENTER)
        if (myBorderless) {
            UIUtil.removeScrollBorder(c)
        }
        revalidate()
        repaint()
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        if (myToolbar != null && myToolbar!!.parent === this && myContent != null && myContent!!.parent === this) {
            g.color = JBColor.border()
            if (myVertical) {
                val y = myContent!!.bounds.maxY.toInt()
                UIUtil.drawLine(g, 0, y, width, y)
            } else {
                val x = myContent!!.bounds.maxX.toInt()
                UIUtil.drawLine(g, x, 0, x, height)
            }
        }
    }
}