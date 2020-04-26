package net.ntworld.mergeRequestIntegrationIde.ui.provider

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.ScrollPaneFactory
import com.intellij.ui.tabs.TabInfo
import com.intellij.ui.tabs.TabsListener
import com.intellij.util.EventDispatcher
import net.ntworld.mergeRequest.MergeRequestInfo
import net.ntworld.mergeRequest.MergeRequestState
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequest.ProviderStatus
import net.ntworld.mergeRequest.api.MergeRequestOrdering
import net.ntworld.mergeRequest.query.GetMergeRequestFilter
import net.ntworld.mergeRequestIntegration.make
import net.ntworld.mergeRequestIntegrationIde.infrastructure.ApplicationService
import net.ntworld.mergeRequestIntegrationIde.ui.mergeRequest.MergeRequestCollectionEventListener
import net.ntworld.mergeRequestIntegrationIde.ui.panel.ProviderInformationPanel
import net.ntworld.mergeRequestIntegrationIde.ui.util.Tabs
import net.ntworld.mergeRequestIntegrationIde.ui.util.TabsUI
import javax.swing.JComponent
import com.intellij.openapi.project.Project as IdeaProject

class ProviderDetails(
    private val applicationService: ApplicationService,
    private val ideaProject: IdeaProject,
    private val toolWindow: ToolWindow,
    private val dispatcher: EventDispatcher<ProviderCollectionListEventListener>,
    private val providerData: ProviderData
) : ProviderDetailsUI {
    override val listEventDispatcher = EventDispatcher.create(MergeRequestCollectionEventListener::class.java)

    private val myListEventListener = object: MergeRequestCollectionEventListener {
        override fun mergeRequestUnselected() {
            listEventDispatcher.multicaster.mergeRequestUnselected()
        }

        override fun mergeRequestSelected(providerData: ProviderData, mergeRequestInfo: MergeRequestInfo) {
            listEventDispatcher.multicaster.mergeRequestSelected(providerData, mergeRequestInfo)
        }
    }

    private val myCommonCenterActionGroupFactory: () -> ActionGroup = {
        DefaultActionGroup()
    }
    private class MyOpenAction(private val self: ProviderDetails):
        AnAction("Open", "Open provider in new tab", AllIcons.Actions.Menu_open) {
        override fun actionPerformed(e: AnActionEvent) {
            self.dispatcher.multicaster.providerOpened(self.providerData)
        }

        override fun update(e: AnActionEvent) {
            e.presentation.isVisible = self.providerData.status == ProviderStatus.ACTIVE
        }

        override fun displayTextInToolbar(): Boolean = true
    }
    private val myCommonSideComponentFactory: () -> JComponent = {
        val actionGroup = DefaultActionGroup()
        actionGroup.add(MyOpenAction(this))

        val toolbar = ActionManager.getInstance().createActionToolbar(
            "${ProviderDetails::class.java.canonicalName}/toolbar-right", actionGroup, true
        )
        toolbar.component
    }
    private val myTabs: TabsUI by lazy {
        val tabs = Tabs(ideaProject, toolWindow.contentManager)
        tabs.setCommonCenterActionGroupFactory(myCommonCenterActionGroupFactory)
        tabs.setCommonSideComponentFactory(myCommonSideComponentFactory)
        tabs
    }
    private val myDetailsTabInfo by lazy {
        val tabInfo = TabInfo(
            ScrollPaneFactory.createScrollPane(ProviderInformationPanel(applicationService, providerData).createComponent())
        )
        tabInfo.text = "General Information"

        tabInfo
    }

    private val myOpeningMRList by lazy {
        val list = ProviderDetailsMRList(
            applicationService, ideaProject, providerData,
            GetMergeRequestFilter.make(
                state = MergeRequestState.OPENED,
                search = "",
                authorId = "",
                assigneeId = "",
                approverIds = listOf()
            ),
            MergeRequestOrdering.RECENTLY_UPDATED,
            displayType = ProviderDetailsMRList.ApprovalStatusDisplayType.NONE
        )
        list.eventDispatcher.addListener(myListEventListener)
        list
    }
    private val myOpeningMRTabInfo by lazy {
        val tabInfo = TabInfo(myOpeningMRList.createComponent())
        tabInfo.text = "Opening MRs"

        tabInfo
    }

    private val myMyMRList by lazy {
        val list = ProviderDetailsMRList(
            applicationService, ideaProject, providerData,
            GetMergeRequestFilter.make(
                state = MergeRequestState.OPENED,
                search = "",
                authorId = providerData.currentUser.id,
                assigneeId = "",
                approverIds = listOf()
            ),
            MergeRequestOrdering.RECENTLY_UPDATED,
            displayType = ProviderDetailsMRList.ApprovalStatusDisplayType.STATUSES
        )
        list.eventDispatcher.addListener(myListEventListener)
        list
    }
    private val myMyMRTabInfo by lazy {
        val tabInfo = TabInfo(myMyMRList.createComponent())
        tabInfo.text = "My MRs"

        tabInfo
    }

    private val myMyAssignedMRList by lazy {
        val list = ProviderDetailsMRList(
            applicationService, ideaProject, providerData,
            GetMergeRequestFilter.make(
                state = MergeRequestState.OPENED,
                search = "",
                authorId = "",
                assigneeId = providerData.currentUser.id,
                approverIds = listOf()
            ),
            MergeRequestOrdering.RECENTLY_UPDATED,
            displayType = ProviderDetailsMRList.ApprovalStatusDisplayType.STATUSES
        )
        list.eventDispatcher.addListener(myListEventListener)
        list
    }
    private val myMyAssignedMRTabInfo by lazy {
        val tabInfo = TabInfo(myMyAssignedMRList.createComponent())
        tabInfo.text = "MRs assigned to me"

        tabInfo
    }

    private val myWaitingForMyApprovalMRList by lazy {
        val list = ProviderDetailsMRList(
            applicationService, ideaProject, providerData,
            GetMergeRequestFilter.make(
                state = MergeRequestState.OPENED,
                search = "",
                authorId = "",
                assigneeId = "",
                approverIds = listOf(providerData.currentUser.id)
            ),
            MergeRequestOrdering.RECENTLY_UPDATED,
            displayType = ProviderDetailsMRList.ApprovalStatusDisplayType.STATUSES_AND_MINE_APPROVAL
        )
        list.eventDispatcher.addListener(myListEventListener)
        list
    }
    private val myWaitingForMyApprovalMRTabInfo by lazy {
        val tabInfo = TabInfo(myWaitingForMyApprovalMRList.createComponent())
        tabInfo.text = "MRs waiting for my approval"

        tabInfo
    }

    init {
        myTabs.addTab(myDetailsTabInfo)
        myTabs.addTab(myOpeningMRTabInfo)
        myTabs.addTab(myMyMRTabInfo)
        if (providerData.hasAssigneeFeature) {
            myTabs.addTab(myMyAssignedMRTabInfo)
        }
        if (providerData.hasApprovalFeature) {
            myTabs.addTab(myWaitingForMyApprovalMRTabInfo)
        }

        myTabs.addListener(object : TabsListener {
            override fun selectionChanged(oldSelection: TabInfo?, newSelection: TabInfo?) {
                if (newSelection === myOpeningMRTabInfo) {
                    myOpeningMRList.fetchIfNotLoaded()
                    return
                }
                if (newSelection === myMyMRTabInfo) {
                    myMyMRList.fetchData()
                    return
                }
                if (providerData.hasAssigneeFeature && newSelection === myMyAssignedMRTabInfo) {
                    myMyAssignedMRList.fetchData()
                    return
                }
                if (providerData.hasApprovalFeature && newSelection === myWaitingForMyApprovalMRTabInfo) {
                    myWaitingForMyApprovalMRList.fetchData()
                    return
                }
            }
        })
    }

    override fun hide() {
        myTabs.component.isVisible = false
    }

    override fun show() {
        myTabs.component.isVisible = true
    }

    override fun createComponent(): JComponent = myTabs.component
}

