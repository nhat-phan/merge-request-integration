package net.ntworld.mergeRequestIntegrationIde.ui.provider

import com.intellij.ui.ScrollPaneFactory
import com.intellij.ui.components.JBList
import com.intellij.util.EventDispatcher
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequestIntegrationIde.ui.panel.ProviderItemPanel
import java.awt.Component
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*

class ProviderCollectionList : ProviderCollectionListUI {
    override val eventDispatcher = EventDispatcher.create(ProviderCollectionListEventListener::class.java)
    private val myProviderDataMap = mutableMapOf<String, ProviderData>()
    private val myList = JBList<ProviderData?>()
    private val myProviderItemPanels = mutableMapOf<Int, ProviderItemPanel>()
    private val myCellRenderer = object: ListCellRenderer<ProviderData?> {
        override fun getListCellRendererComponent(
            list: JList<out ProviderData?>?,
            value: ProviderData?,
            index: Int,
            isSelected: Boolean,
            cellHasFocus: Boolean
        ): Component {
            if (null === value) {
                return JPanel()
            }
            if (null === myProviderItemPanels[index]) {
                myProviderItemPanels[index] = ProviderItemPanel(value)
            }
            val panel = myProviderItemPanels[index]!!
            panel.changeStyle(isSelected, cellHasFocus)
            return panel.createComponent()
        }
    }
    private val myListMouseListener = object: MouseAdapter() {
        override fun mouseClicked(e: MouseEvent?) {
            if (null === e) {
                return
            }
            if (e.clickCount == 1) {
                val value = myList.selectedValue
                if (null === value) {
                    eventDispatcher.multicaster.providerUnselected()
                } else {
                    eventDispatcher.multicaster.providerSelected(value)
                }
            }
            if (e.clickCount == 2) {
                val value = myList.selectedValue
                if (null === value) {
                    eventDispatcher.multicaster.providerUnselected()
                } else {
                    eventDispatcher.multicaster.providerOpened(value)
                }
            }
        }
    }

    init {
        myProviderItemPanels.clear()
        myProviderDataMap.clear()
        myList.clearSelection()
        myList.selectionMode = ListSelectionModel.SINGLE_SELECTION
        myList.cellRenderer = myCellRenderer
        myList.addMouseListener(myListMouseListener)
        updateList()
    }

    private fun updateList() {
        myProviderItemPanels.clear()
        val data = myProviderDataMap.values.sortedBy {
            it.name
        }
        myList.setListData(data.toTypedArray())
    }

    override fun clear() {
        myProviderItemPanels.clear()
        myProviderDataMap.clear()
        myList.clearSelection()
        updateList()
    }

    override fun addProvider(providerData: ProviderData) {
        myProviderDataMap[providerData.id] = providerData
        updateList()
    }

    override fun setProviders(providerDataCollection: List<ProviderData>) {
        providerDataCollection.forEach { myProviderDataMap[it.id] = it }
        updateList()
    }

    override fun createComponent(): JComponent = ScrollPaneFactory.createScrollPane(myList)
}