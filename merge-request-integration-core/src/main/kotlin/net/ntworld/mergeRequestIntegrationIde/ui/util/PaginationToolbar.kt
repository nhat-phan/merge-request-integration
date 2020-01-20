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

    private val myRefreshButtonAction = object : AnAction("Refresh", "Refresh", AllIcons.Actions.Refresh) {
        override fun actionPerformed(e: AnActionEvent) {
            eventDispatcher.multicaster.changePage(myPage)
        }

        override fun update(e: AnActionEvent) {
            e.presentation.isEnabled = myEnabled && myIsValid
        }
    }

    private val myFirstAction = object : AnAction("First", "Go to the first page", AllIcons.Actions.Play_first) {
        override fun actionPerformed(e: AnActionEvent) {
            setData(1, myTotalPages, myTotalItems)
            eventDispatcher.multicaster.changePage(myPage)
        }

        override fun update(e: AnActionEvent) {
            e.presentation.isEnabled = myEnabled && myIsValid && myPage != 1
        }
    }

    private val myPrevAction = object : AnAction("Previous", "Previous page", AllIcons.Actions.Play_back) {
        override fun actionPerformed(e: AnActionEvent) {
            setData(myPage - 1, myTotalPages, myTotalItems)
            eventDispatcher.multicaster.changePage(myPage)
        }

        override fun update(e: AnActionEvent) {
            e.presentation.isEnabled = myEnabled && myIsValid && myPage > 1 && myTotalPages > 1
        }
    }

    private val myNextAction = object : AnAction("Next", "Next page", AllIcons.Actions.Play_forward) {
        override fun actionPerformed(e: AnActionEvent) {
            setData(myPage + 1, myTotalPages, myTotalItems)
            eventDispatcher.multicaster.changePage(myPage)
        }

        override fun update(e: AnActionEvent) {
            e.presentation.isEnabled = myEnabled && myIsValid && myPage < myTotalPages
        }
    }

    private val myLastAction = object : AnAction("Last", "Go to the last page", AllIcons.Actions.Play_last) {
        override fun actionPerformed(e: AnActionEvent) {
            setData(myTotalPages, myTotalPages, myTotalItems)
            eventDispatcher.multicaster.changePage(myPage)
        }

        override fun update(e: AnActionEvent) {
            e.presentation.isEnabled = myEnabled && myIsValid && myPage < myTotalPages
        }
    }

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