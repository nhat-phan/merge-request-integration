package net.ntworld.mergeRequestIntegrationIde.ui.mergeRequest

import com.intellij.openapi.project.Project as IdeaProject
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.util.EventDispatcher
import net.ntworld.mergeRequest.MergeRequest
import net.ntworld.mergeRequest.MergeRequestInfo
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequest.api.MergeRequestOrdering
import net.ntworld.mergeRequest.query.GetMergeRequestFilter
import net.ntworld.mergeRequestIntegrationIde.service.ApplicationService
import net.ntworld.mergeRequestIntegrationIde.service.ProjectEventListener
import net.ntworld.mergeRequestIntegrationIde.service.ProjectService
import net.ntworld.mergeRequestIntegrationIde.ui.Component
import javax.swing.JComponent

class MergeRequestCollection(
    private val applicationService: ApplicationService,
    private val ideaProject: IdeaProject,
    private val providerData: ProviderData
) : MergeRequestCollectionUI {
    private val myComponent = SimpleToolWindowPanel(true, true)
    private val myFilter: MergeRequestCollectionFilterUI by lazy {
        MergeRequestCollectionFilter(applicationService, ideaProject, providerData)
    }
    private val myTree: MergeRequestCollectionUI by lazy {
        val tree = MergeRequestCollectionTree(applicationService, ideaProject, providerData)
        if (applicationService.settings.saveMRFilterState) {
            val projectService = applicationService.getProjectService(ideaProject)
            val pair = projectService.findFiltersByProviderId(providerData.key)
            tree.setFilter(pair.first)
            tree.setOrder(pair.second)
        }

        tree.fetchData()
        tree
    }
    private val myFilterListener = object: MergeRequestCollectionFilterEventListener {
        override fun filterChanged(filter: GetMergeRequestFilter) {
            myTree.setFilter(filter)
            myTree.fetchData()
        }

        override fun orderChanged(order: MergeRequestOrdering) {
            myTree.setOrder(order)
            myTree.fetchData()
        }
    }
    private val myProjectEventListener = object: ProjectEventListener {
        override fun startCodeReview(providerData: ProviderData, mergeRequest: MergeRequest) {
            myComponent.isVisible = false
        }

        override fun stopCodeReview(providerData: ProviderData, mergeRequest: MergeRequest) {
            myComponent.isVisible = true
        }
    }

    init {
        myComponent.toolbar = myFilter.createComponent()
        myComponent.setContent(myTree.createComponent())
        myFilter.eventDispatcher.addListener(myFilterListener)
        myTree.eventDispatcher.addListener(object: MergeRequestCollectionEventListener {
            override fun mergeRequestUnselected() {
            }

            override fun mergeRequestSelected(providerData: ProviderData, mergeRequestInfo: MergeRequestInfo) {
            }
        })
        applicationService.getProjectService(ideaProject).dispatcher.addListener(myProjectEventListener)
    }

    override val eventDispatcher = myTree.eventDispatcher

    override fun setFilter(filter: GetMergeRequestFilter) {
        myTree.setFilter(filter)
    }

    override fun setOrder(ordering: MergeRequestOrdering) {
        myTree.setOrder(ordering)
    }

    override fun fetchData() {
        myTree.fetchData()
    }

    override fun createComponent(): JComponent = myComponent
}