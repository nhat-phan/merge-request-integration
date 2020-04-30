package net.ntworld.mergeRequestIntegrationIde.ui.mergeRequest

import com.intellij.openapi.Disposable
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.util.Disposer
import net.ntworld.mergeRequest.MergeRequestInfo
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequest.api.MergeRequestOrdering
import net.ntworld.mergeRequest.query.GetMergeRequestFilter
import net.ntworld.mergeRequestIntegrationIde.infrastructure.ProjectServiceProvider
import net.ntworld.mergeRequestIntegrationIde.infrastructure.ReviewContext
import net.ntworld.mergeRequestIntegrationIde.infrastructure.notifier.ProjectNotifier
import net.ntworld.mergeRequestIntegrationIde.infrastructure.notifier.ProjectNotifierAdapter
import javax.swing.JComponent

class MergeRequestCollection(
    private val projectServiceProvider: ProjectServiceProvider,
    private val providerData: ProviderData
) : MergeRequestCollectionUI, Disposable {
    private val myComponent = SimpleToolWindowPanel(true, true)
    private val myFilter: MergeRequestCollectionFilterUI by lazy {
        MergeRequestCollectionFilter(projectServiceProvider, providerData)
    }
    private val myTree: MergeRequestCollectionUI by lazy {
        val tree = MergeRequestCollectionTree(projectServiceProvider, providerData)
        if (projectServiceProvider.applicationSettings.saveMRFilterState) {
            val pair = projectServiceProvider.filtersStorage.find(providerData.key)
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
    private val myProjectNotifier = object : ProjectNotifierAdapter() {
        override fun startCodeReview(reviewContext: ReviewContext) {
            myComponent.isVisible = false
        }

        override fun stopCodeReview(reviewContext: ReviewContext) {
            myComponent.isVisible = true
        }
    }
    private val myConnection = projectServiceProvider.messageBus.connect()

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
        myConnection.subscribe(ProjectNotifier.TOPIC, myProjectNotifier)
        Disposer.register(projectServiceProvider.project, this)
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

    override fun dispose() {
        myConnection.disconnect()
    }
}