package net.ntworld.mergeRequestIntegrationIde.ui.mergeRequest

import com.intellij.icons.AllIcons
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.application.ApplicationManager
import com.intellij.ui.ScrollPaneFactory
import com.intellij.ui.tabs.TabInfo
import net.ntworld.mergeRequest.*
import net.ntworld.mergeRequestIntegrationIde.mergeRequest.comments.CommentsTabFactory
import net.ntworld.mergeRequestIntegrationIde.mergeRequest.comments.CommentsTabPresenter
import net.ntworld.mergeRequestIntegrationIde.service.ApplicationService
import net.ntworld.mergeRequestIntegrationIde.service.CommentStore
import net.ntworld.mergeRequestIntegrationIde.service.ProjectEventListener
import net.ntworld.mergeRequestIntegrationIde.task.*
import com.intellij.openapi.project.Project as IdeaProject
import net.ntworld.mergeRequestIntegrationIde.ui.mergeRequest.tab.*
import net.ntworld.mergeRequestIntegrationIde.ui.service.FetchService
import net.ntworld.mergeRequestIntegrationIde.component.Icons
import net.ntworld.mergeRequestIntegrationIde.infrastructure.ReviewContextManager
import net.ntworld.mergeRequestIntegrationIde.ui.util.Tabs
import net.ntworld.mergeRequestIntegrationIde.ui.util.TabsUI
import javax.swing.JComponent

class MergeRequestDetails(
    private val applicationService: ApplicationService,
    private val ideaProject: IdeaProject,
    private val disposable: Disposable,
    private val providerData: ProviderData
) : MergeRequestDetailsUI {
    private val myToolbars = mutableListOf<MergeRequestDetailsToolbarUI>()

    private val myInfoTab : MergeRequestInfoTabUI = MergeRequestInfoTab()
    private val myInfoTabInfo: TabInfo by lazy {
        val tabInfo = TabInfo(
            ScrollPaneFactory.createScrollPane(myInfoTab.createComponent())
        )
        tabInfo.text = "Info"
        tabInfo.icon = AllIcons.General.ShowInfos

        tabInfo
    }

    private val myDescriptionTab: MergeRequestDescriptionTabUI = MergeRequestDescriptionTab()
    private val myDescriptionTabInfo: TabInfo by lazy {
        val tabInfo = TabInfo(
            ScrollPaneFactory.createScrollPane(myDescriptionTab.createComponent())
        )
        tabInfo.text = "Description"
        tabInfo.icon = Icons.Description

        tabInfo
    }

    private val myCommitsTab: MergeRequestCommitsTabUI = MergeRequestCommitsTab(applicationService, ideaProject)
    private val myCommitsTabInfo: TabInfo by lazy {
        val tabInfo = TabInfo(myCommitsTab.createComponent())
        tabInfo.text = "Commit"
        tabInfo.icon = AllIcons.Vcs.CommitNode

        tabInfo
    }

    private val myLegacyCommentsTab: MergeRequestCommentsTabUI = MergeRequestCommentsTab(applicationService, ideaProject)
    private val myLegacyCommentsTabInfo: TabInfo by lazy {
        val tabInfo = TabInfo(myLegacyCommentsTab.createComponent())
        tabInfo.text = "Comments"
        tabInfo.icon = Icons.Comments

        tabInfo
    }
    private val myCommentsTabListener = object: MergeRequestCommentsTabUI.Listener {
        override fun commentsDisplayed(total: Int) {
            displayCommentsCount(total)
        }

        override fun refreshRequested(mergeRequest: MergeRequest) {
            GetLegacyCommentsTask(applicationService, ideaProject, providerData, mergeRequest, myGetCommentsListener).start()
        }
    }

    private val myCommentsTabPresenter: CommentsTabPresenter by lazy {
        val projectService = applicationService.getProjectService(ideaProject)
        val model = CommentsTabFactory.makeCommentsTabModel(projectService, providerData)
        val view = CommentsTabFactory.makeCommentsTabView(projectService)
        CommentsTabFactory.makeCommentsTabPresenter(
            applicationService, projectService, model, view
        )
    }

    private val myTabs: TabsUI by lazy {
        val tabs = Tabs(ideaProject, disposable)
        tabs.setCommonCenterActionGroupFactory(this::toolbarCommonCenterActionGroupFactory)
        tabs.setCommonSideComponentFactory(this::toolbarCommonSideComponentFactory)
        tabs
    }

    private val myFindMRListener = object : FindMergeRequestTask.Listener {
        override fun dataReceived(mergeRequest: MergeRequest) {
            setMergeRequest(mergeRequest)
        }
    }
    private val myGetPipelinesListener = object : GetPipelinesTask.Listener {
        override fun onError(exception: Exception) {
            println(exception)
        }

        override fun dataReceived(mergeRequestInfo: MergeRequestInfo, pipelines: List<Pipeline>) {
            ApplicationManager.getApplication().invokeLater {
                myToolbars.forEach {
                    it.setPipelines(mergeRequestInfo, pipelines)
                }
            }
        }
    }
    private val myGetCommitsListener = object : GetCommitsTask.Listener {
        override fun onError(exception: Exception) {
            println(exception)
        }

        override fun taskStarted() {
            myCommitsTab.clear()
        }

        override fun dataReceived(mergeRequestInfo: MergeRequestInfo, commits: List<Commit>) {
            ApplicationManager.getApplication().invokeLater {
                myToolbars.forEach {
                    it.setCommits(mergeRequestInfo, commits)
                }
                myCommitsTab.setCommits(providerData, mergeRequestInfo, commits)
                if (commits.isEmpty()) {
                    myCommitsTabInfo.text = "Commits"
                } else {
                    myCommitsTabInfo.text = "Commits · ${commits.size}"
                }
            }
        }
    }
    private val mySelectCommitsListener = object: MergeRequestCommitsTabUI.Listener {
        override fun commitSelected(
            providerData: ProviderData,
            mergeRequestInfo: MergeRequestInfo,
            commits: Collection<Commit>
        ) {
            myToolbars.forEach {
                it.setCommitsForReviewing(mergeRequestInfo, commits.toList())
            }
        }
    }

    private val myGetCommentsListener = object: GetLegacyCommentsTask.Listener {
        override fun onError(exception: Exception) {
            println(exception)
        }

        override fun dataReceived(providerData: ProviderData, mergeRequest: MergeRequest, comments: List<Comment>) {
            ApplicationManager.getApplication().invokeLater {
                myToolbars.forEach {
                    it.setComments(mergeRequest, comments)
                }
                myLegacyCommentsTab.setComments(providerData, mergeRequest, comments)
            }
        }
    }
    private val myFindApprovalListener = object : FindApprovalTask.Listener {
        override fun onError(exception: Exception) {
            println(exception)
        }

        override fun dataReceived(mergeRequestInfo: MergeRequestInfo, approval: Approval) {
            ApplicationManager.getApplication().invokeLater {
                myToolbars.forEach {
                    it.setApproval(mergeRequestInfo, approval)
                }
            }
        }
    }

    private val myProjectEventListener = object : ProjectEventListener {
        override fun newCommentRequested(
            providerData: ProviderData,
            mergeRequest: MergeRequest,
            position: CommentPosition,
            item: CommentStore.Item
        ) {
            myTabs.getTabs().select(myLegacyCommentsTabInfo, true)
        }

        override fun displayCommentRequested(comment: Comment) {
            if (applicationService.getProjectService(ideaProject).isDoingCodeReview()) {
                myTabs.getTabs().select(myLegacyCommentsTabInfo, true)
            }
        }
    }

    private val myToolbarListener = object : MergeRequestDetailsToolbarUI.Listener {
        override fun refreshRequested(mergeRequestInfo: MergeRequestInfo) {
            setMergeRequestInfo(mergeRequestInfo)
        }
    }

    init {
        myTabs.addTab(myInfoTabInfo)
        myTabs.addTab(myDescriptionTabInfo)
        myTabs.addTab(myLegacyCommentsTabInfo)
        myTabs.addTab(myCommentsTabPresenter.tabInfo)
        myTabs.addTab(myCommitsTabInfo)

        myCommitsTab.dispatcher.addListener(mySelectCommitsListener)
        myLegacyCommentsTab.dispatcher.addListener(myCommentsTabListener)
        applicationService.getProjectService(ideaProject).dispatcher.addListener(myProjectEventListener)
    }

    override fun hide() {
        myTabs.component.isVisible = false
    }

    override fun setMergeRequest(mergeRequest: MergeRequest) {
        GetLegacyCommentsTask(applicationService, ideaProject, providerData, mergeRequest, myGetCommentsListener).start()
        myInfoTab.setMergeRequest(mergeRequest)
        myToolbars.forEach {
            it.setMergeRequest(mergeRequest)
        }
    }

    override fun setMergeRequestInfo(mergeRequestInfo: MergeRequestInfo) {
        ReviewContextManager.initContext(ideaProject, providerData, mergeRequestInfo, true)
        FetchService.start(applicationService, ideaProject, providerData, mergeRequestInfo)

        myInfoTab.setMergeRequestInfo(mergeRequestInfo)
        myDescriptionTab.setMergeRequestInfo(providerData, mergeRequestInfo)
        myLegacyCommentsTabInfo.text = "Comments"

        myCommentsTabPresenter.model.mergeRequestInfo = mergeRequestInfo

        myTabs.getTabs().select(myInfoTabInfo, false)
        myTabs.component.isVisible = true
        myToolbars.forEach {
            it.setMergeRequestInfo(mergeRequestInfo)
        }
        FindMergeRequestTask(applicationService, ideaProject, providerData, mergeRequestInfo, myFindMRListener)
            .start()
        GetPipelinesTask(applicationService, ideaProject, providerData, mergeRequestInfo, myGetPipelinesListener)
            .start()
        GetCommitsTask(applicationService, ideaProject, providerData, mergeRequestInfo, myGetCommitsListener)
            .start()
        if (providerData.hasApprovalFeature) {
            FindApprovalTask(applicationService, ideaProject, providerData, mergeRequestInfo, myFindApprovalListener)
                .start()
        }
    }

    override fun createComponent(): JComponent = myTabs.component

    private fun displayCommentsCount(count: Int) {
        if (count == 0) {
            myLegacyCommentsTabInfo.text = "Comments"
        } else {
            myLegacyCommentsTabInfo.text = "Comments · $count"
        }
    }

    private fun toolbarCommonCenterActionGroupFactory(): ActionGroup {
        return DefaultActionGroup()
    }

    private fun toolbarCommonSideComponentFactory(): JComponent {
        val toolbar: MergeRequestDetailsToolbarUI = MergeRequestDetailsToolbar(
            applicationService, ideaProject, providerData, this
        )
        toolbar.dispatcher.addListener(myToolbarListener)
        myToolbars.add(toolbar)
        return toolbar.createComponent()
    }
}