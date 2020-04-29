package net.ntworld.mergeRequestIntegrationIde.ui.mergeRequest

import com.intellij.openapi.application.ApplicationManager
import com.intellij.util.EventDispatcher
import net.ntworld.mergeRequest.MergeRequestInfo
import net.ntworld.mergeRequest.MergeRequestState
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequest.ProviderStatus
import net.ntworld.mergeRequest.api.MergeRequestOrdering
import net.ntworld.mergeRequest.query.GetMergeRequestFilter
import net.ntworld.mergeRequestIntegration.make
import net.ntworld.mergeRequestIntegrationIde.component.ComponentFactory
import net.ntworld.mergeRequestIntegrationIde.component.PaginationToolbar
import net.ntworld.mergeRequestIntegrationIde.infrastructure.ProjectServiceProvider
import net.ntworld.mergeRequestIntegrationIde.task.SearchMergeRequestTask
import net.ntworld.mergeRequestIntegrationIde.ui.util.CustomSimpleToolWindowPanel
import javax.swing.JComponent

abstract class AbstractMergeRequestCollection(
    private val projectServiceProvider: ProjectServiceProvider,
    private val providerData: ProviderData
) : MergeRequestCollectionUI {
    abstract fun makeContent(): JComponent

    abstract fun fetchDataStarted()

    abstract fun fetchDataStopped()

    abstract fun dataReceived(collection: List<MergeRequestInfo>)

    open fun fetchDataError() {
    }

    override val eventDispatcher = EventDispatcher.create(MergeRequestCollectionEventListener::class.java)
    private val mySplitter = CustomSimpleToolWindowPanel(vertical = true, borderless = true)
    private val myPaginator: PaginationToolbar = ComponentFactory.makePaginationToolbar(displayRefreshButton = true)
    private val myContentDelegate = lazy {
        makeContent()
    }
    private val myContent: JComponent by myContentDelegate
    private var myFilter = GetMergeRequestFilter.make(
        state = MergeRequestState.OPENED,
        search = "",
        authorId = "",
        assigneeId = "",
        approverIds = listOf()
    )
    private var myOrdering = MergeRequestOrdering.RECENTLY_UPDATED
    private val myListener = object : SearchMergeRequestTask.Listener {
        override fun onError(exception: Exception) {
            myPaginator.enable()
            fetchDataError()
            fetchDataStopped()
        }

        override fun taskStarted() {
            myPaginator.disable()
            fetchDataStarted()
        }
        override fun taskEnded() {
            myPaginator.enable()
            fetchDataStopped()
        }

        override fun dataReceived(list: List<MergeRequestInfo>, page: Int, totalPages: Int, totalItems: Int) {
            ApplicationManager.getApplication().invokeLater {
                dataReceived(list)
                myPaginator.setData(page, totalPages, totalItems)
            }
        }
    }
    private val myPaginatorListener = object: PaginationToolbar.Listener {
        override fun changePage(page: Int) {
            fetchPage(page)
        }
    }

    init {
        myPaginator.addListener(myPaginatorListener)
        mySplitter.toolbar = myPaginator.component
    }

    final override fun setFilter(filter: GetMergeRequestFilter) {
        myFilter = filter
    }

    final override fun setOrder(ordering: MergeRequestOrdering) {
        myOrdering = ordering
    }

    override fun fetchData() = fetchPage(1)

    private fun fetchPage(page: Int) {
        if (providerData.status == ProviderStatus.ACTIVE) {
            val task = SearchMergeRequestTask(
                projectServiceProvider = projectServiceProvider,
                providerData = providerData,
                filtering = myFilter,
                ordering = myOrdering,
                listener = myListener
            )
            task.start(page)
        }
    }

    override fun createComponent(): JComponent {
        if (!myContentDelegate.isInitialized()) {
            mySplitter.setContent(myContent)
        }
        return mySplitter
    }
}