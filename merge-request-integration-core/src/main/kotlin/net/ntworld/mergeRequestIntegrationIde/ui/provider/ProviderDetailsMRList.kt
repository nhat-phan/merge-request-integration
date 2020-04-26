package net.ntworld.mergeRequestIntegrationIde.ui.provider

import com.intellij.ui.ScrollPaneFactory
import com.intellij.ui.components.JBList
import net.ntworld.mergeRequest.MergeRequestInfo
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequest.ProviderStatus
import net.ntworld.mergeRequest.api.MergeRequestOrdering
import net.ntworld.mergeRequest.query.GetMergeRequestFilter
import net.ntworld.mergeRequestIntegrationIde.infrastructure.ProjectServiceProvider
import net.ntworld.mergeRequestIntegrationIde.ui.mergeRequest.AbstractMergeRequestCollection
import net.ntworld.mergeRequestIntegrationIde.ui.panel.MergeRequestItemPanel
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JComponent
import javax.swing.ListCellRenderer
import javax.swing.ListSelectionModel

class ProviderDetailsMRList(
    private val projectServiceProvider: ProjectServiceProvider,
    private val providerData: ProviderData,
    private val filterBy: GetMergeRequestFilter,
    private val orderBy:  MergeRequestOrdering,
    private val displayType: ApprovalStatusDisplayType
): AbstractMergeRequestCollection(projectServiceProvider, providerData) {
    private var isLoaded = false
    private val myList = JBList<MergeRequestInfo>()
    private val myItemPanels = mutableMapOf<Int, MergeRequestItemPanel>()
    private val myCellRenderer = ListCellRenderer<MergeRequestInfo> { list, value, index, isSelected, cellHasFocus ->
        if (null === myItemPanels[index]) {
            myItemPanels[index] = MergeRequestItemPanel(
                projectServiceProvider,
                providerData,
                value,
                displayType
            )
        }
        val panel = myItemPanels[index]!!
        panel.changeStyle(isSelected, cellHasFocus)
        panel.createComponent()
    }
    private val myListMouseListener = object: MouseAdapter() {
        override fun mouseClicked(e: MouseEvent?) {
            if (null === e) {
                return
            }
            if (e.clickCount == 2) {
                val value = myList.selectedValue
                if (null === value) {
                    eventDispatcher.multicaster.mergeRequestUnselected()
                } else {
                    eventDispatcher.multicaster.mergeRequestSelected(providerData, value)
                }
            }
        }
    }

    init {
        myList.selectionMode = ListSelectionModel.SINGLE_SELECTION
        myList.cellRenderer = myCellRenderer
        myList.addMouseListener(myListMouseListener)
    }

    override fun makeContent(): JComponent {
        setFilter(filterBy)
        setOrder(orderBy)

        return ScrollPaneFactory.createScrollPane(myList)
    }

    fun fetchIfNotLoaded() {
        if (!isLoaded && providerData.status == ProviderStatus.ACTIVE) {
            fetchData()
            isLoaded = true
        }
    }

    override fun fetchDataStarted() {
        myList.isVisible = false
    }

    override fun fetchDataStopped() {
        myList.isVisible = true
    }

    override fun dataReceived(collection: List<MergeRequestInfo>) {
        myItemPanels.clear()
        myList.setListData(collection.toTypedArray())
        myList.isVisible = true
    }

    enum class ApprovalStatusDisplayType {
        NONE,
        STATUSES_AND_MINE_APPROVAL,
        STATUSES
    }
}