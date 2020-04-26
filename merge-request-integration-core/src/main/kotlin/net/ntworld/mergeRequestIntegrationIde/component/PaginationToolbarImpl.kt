package net.ntworld.mergeRequestIntegrationIde.component

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import net.miginfocom.swing.MigLayout
import com.intellij.util.EventDispatcher
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel

internal class PaginationToolbarImpl(private val displayRefreshButton: Boolean = false) : PaginationToolbar {
    private val dispatcher = EventDispatcher.create(PaginationToolbar.Listener::class.java)

    private var myEnabled: Boolean = true
    private var myIsValid: Boolean = true
    private var myPage: Int = 0
    private var myTotalPages: Int = 0
    private var myTotalItems: Int = 0

    private val myRefreshButtonAction = MyRefreshButtonAction(this)
    private val myFirstAction = MyFirstAction(this)
    private val myPrevAction = MyPrevAction(this)
    private val myNextAction = MyNextAction(this)
    private val myLastAction = MyLastAction(this)

    private val myInfo = JLabel()
    private val myPanel by lazy {
        val panel = JPanel(MigLayout("ins 0, fill", "[left, fill]push[right]", "center"))

        val actionGroup = DefaultActionGroup()
        if (displayRefreshButton) {
            actionGroup.add(myRefreshButtonAction)
            actionGroup.addSeparator()
        }
        actionGroup.add(myFirstAction)
        actionGroup.add(myPrevAction)
        actionGroup.add(myNextAction)
        actionGroup.add(myLastAction)

        val toolbar = ActionManager.getInstance().createActionToolbar(
            "${this::class.java.canonicalName}/toolbar-right", actionGroup, true
        )

        panel.add(myInfo)
        panel.add(toolbar.component)
        panel
    }

    override val component: JComponent = myPanel

    override fun enable() {
        myEnabled = true
        updateInfo()
    }

    override fun disable() {
        myEnabled = false
        updateInfo()
    }

    override fun getCurrentPage(): Int {
        return if (myPage <= 0) 1 else myPage
    }

    override fun setData(page: Int, totalPages: Int, totalItems: Int) {
        myIsValid = true
        if (page <= 0 || totalPages <= 0 || totalItems <= 0) {
            myIsValid = false
        }

        if (page > totalPages) {
            myIsValid = false
        }

        myPage = page
        myTotalPages = totalPages
        myTotalItems = totalItems
        updateInfo()
    }

    override fun addListener(listener: PaginationToolbar.Listener) {
        dispatcher.addListener(listener)
    }

    override fun removeListener(listener: PaginationToolbar.Listener) {
        dispatcher.removeListener(listener)
    }

    private fun updateInfo() {
        if (myEnabled) {
            myInfo.text = " Displaying page $myPage/$myTotalPages. Total items: $myTotalItems"
        } else {
            myInfo.text = " Loading data..."
        }
    }

    private class MyRefreshButtonAction(private val self: PaginationToolbarImpl) :
        AnAction("Refresh", "Refresh", AllIcons.Actions.Refresh) {

        override fun actionPerformed(e: AnActionEvent) {
            self.dispatcher.multicaster.changePage(self.myPage)
        }

        override fun update(e: AnActionEvent) {
            e.presentation.isEnabled = self.myEnabled && self.myIsValid
        }
    }

    private class MyFirstAction(private val self: PaginationToolbarImpl) :
        AnAction("First", "Go to the first page", AllIcons.Actions.Play_first) {

        override fun actionPerformed(e: AnActionEvent) {
            self.setData(1, self.myTotalPages, self.myTotalItems)
            self.dispatcher.multicaster.changePage(self.myPage)
        }

        override fun update(e: AnActionEvent) {
            e.presentation.isEnabled = self.myEnabled && self.myIsValid && self.myPage != 1
        }
    }

    private class MyPrevAction(private val self: PaginationToolbarImpl) :
        AnAction("Previous", "Previous page", AllIcons.Actions.Play_back) {

        override fun actionPerformed(e: AnActionEvent) {
            self.setData(self.myPage - 1, self.myTotalPages, self.myTotalItems)
            self.dispatcher.multicaster.changePage(self.myPage)
        }

        override fun update(e: AnActionEvent) {
            e.presentation.isEnabled = self.myEnabled && self.myIsValid && self.myPage > 1 && self.myTotalPages > 1
        }
    }

    private class MyNextAction(private val self: PaginationToolbarImpl) :
        AnAction("Next", "Next page", AllIcons.Actions.Play_forward) {

        override fun actionPerformed(e: AnActionEvent) {
            self.setData(self.myPage + 1, self.myTotalPages, self.myTotalItems)
            self.dispatcher.multicaster.changePage(self.myPage)
        }

        override fun update(e: AnActionEvent) {
            e.presentation.isEnabled = self.myEnabled && self.myIsValid && self.myPage < self.myTotalPages
        }
    }

    private class MyLastAction(private val self: PaginationToolbarImpl) :
        AnAction("Last", "Go to the last page", AllIcons.Actions.Play_last) {

        override fun actionPerformed(e: AnActionEvent) {
            self.setData(self.myTotalPages, self.myTotalPages, self.myTotalItems)
            self.dispatcher.multicaster.changePage(self.myPage)
        }

        override fun update(e: AnActionEvent) {
            e.presentation.isEnabled = self.myEnabled && self.myIsValid && self.myPage < self.myTotalPages
        }
    }
}
