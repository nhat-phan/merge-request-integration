package net.ntworld.mergeRequestIntegrationIde.ui.util

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.util.EventDispatcher
import net.miginfocom.swing.MigLayout
import net.ntworld.mergeRequestIntegrationIde.ui.provider.ProviderCollectionToolbar
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel

class PaginationToolbar(private val allowRefreshBtn: Boolean = false) : PaginationToolbarUI {
    override val eventDispatcher = EventDispatcher.create(PaginationToolbarUI.PaginationEventListener::class.java)

    private var myEnabled: Boolean = true
    private var myIsValid: Boolean = true
    private var myPage: Int = 0
    private var myTotalPages: Int = 0
    private var myTotalItems: Int = 0

    private class MyRefreshButtonAction(private val self: PaginationToolbar) :
        AnAction("Refresh", "Refresh", AllIcons.Actions.Refresh) {

        override fun actionPerformed(e: AnActionEvent) {
            self.eventDispatcher.multicaster.changePage(self.myPage)
        }

        override fun update(e: AnActionEvent) {
            e.presentation.isEnabled = self.myEnabled && self.myIsValid
        }
    }
    private val myRefreshButtonAction = MyRefreshButtonAction(this)

    private class MyFirstAction(private val self: PaginationToolbar) :
        AnAction("First", "Go to the first page", AllIcons.Actions.Play_first) {
        override fun actionPerformed(e: AnActionEvent) {
            self.setData(1, self.myTotalPages, self.myTotalItems)
            self.eventDispatcher.multicaster.changePage(self.myPage)
        }

        override fun update(e: AnActionEvent) {
            e.presentation.isEnabled = self.myEnabled && self.myIsValid && self.myPage != 1
        }
    }
    private val myFirstAction = MyFirstAction(this)

    private class MyPrevAction(private val self: PaginationToolbar) :
        AnAction("Previous", "Previous page", AllIcons.Actions.Play_back) {
        override fun actionPerformed(e: AnActionEvent) {
            self.setData(self.myPage - 1, self.myTotalPages, self.myTotalItems)
            self.eventDispatcher.multicaster.changePage(self.myPage)
        }

        override fun update(e: AnActionEvent) {
            e.presentation.isEnabled = self.myEnabled && self.myIsValid && self.myPage > 1 && self.myTotalPages > 1
        }
    }
    private val myPrevAction = MyPrevAction(this)

    private class MyNextAction(private val self: PaginationToolbar) :
        AnAction("Next", "Next page", AllIcons.Actions.Play_forward) {
        override fun actionPerformed(e: AnActionEvent) {
            self.setData(self.myPage + 1, self.myTotalPages, self.myTotalItems)
            self.eventDispatcher.multicaster.changePage(self.myPage)
        }

        override fun update(e: AnActionEvent) {
            e.presentation.isEnabled = self.myEnabled && self.myIsValid && self.myPage < self.myTotalPages
        }
    }
    private val myNextAction = MyNextAction(this)

    private class MyLastAction(private val self: PaginationToolbar) :
        AnAction("Last", "Go to the last page", AllIcons.Actions.Play_last) {
        override fun actionPerformed(e: AnActionEvent) {
            self.setData(self.myTotalPages, self.myTotalPages, self.myTotalItems)
            self.eventDispatcher.multicaster.changePage(self.myPage)
        }

        override fun update(e: AnActionEvent) {
            e.presentation.isEnabled = self.myEnabled && self.myIsValid && self.myPage < self.myTotalPages
        }
    }
    private val myLastAction = MyLastAction(this)

    private val myInfo = JLabel()

    private val myPanel by lazy {
        val panel = JPanel(MigLayout("ins 0, fill", "[left, fill]push[right]", "center"))

        val actionGroup = DefaultActionGroup()
        if (allowRefreshBtn) {
            actionGroup.add(myRefreshButtonAction)
            actionGroup.addSeparator()
        }
        actionGroup.add(myFirstAction)
        actionGroup.add(myPrevAction)
        actionGroup.add(myNextAction)
        actionGroup.add(myLastAction)

        val toolbar = ActionManager.getInstance().createActionToolbar(
            "${ProviderCollectionToolbar::class.java.canonicalName}/toolbar-right", actionGroup, true
        )

        panel.add(myInfo)
        panel.add(toolbar.component)
        panel
    }

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

    private fun updateInfo() {
        if (myEnabled) {
            myInfo.text = " Displaying page $myPage/$myTotalPages. Total items: $myTotalItems"
        } else {
            myInfo.text = " Loading data..."
        }
    }

    override fun createComponent(): JComponent = myPanel
}