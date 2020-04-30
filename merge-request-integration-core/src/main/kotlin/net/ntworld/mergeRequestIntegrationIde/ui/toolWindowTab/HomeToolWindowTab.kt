package net.ntworld.mergeRequestIntegrationIde.ui.toolWindowTab

import com.intellij.openapi.Disposable
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.OnePixelSplitter
import com.intellij.ui.content.Content
import com.intellij.ui.content.ContentManagerEvent
import com.intellij.ui.content.ContentManagerListener
import net.ntworld.mergeRequest.MergeRequestInfo
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequestIntegrationIde.infrastructure.ProjectServiceProvider
import net.ntworld.mergeRequestIntegrationIde.infrastructure.ReviewContext
import net.ntworld.mergeRequestIntegrationIde.infrastructure.notifier.ProjectNotifier
import net.ntworld.mergeRequestIntegrationIde.infrastructure.notifier.ProjectNotifierAdapter
import net.ntworld.mergeRequestIntegrationIde.ui.Component
import net.ntworld.mergeRequestIntegrationIde.ui.mergeRequest.MergeRequestCollectionEventListener
import net.ntworld.mergeRequestIntegrationIde.ui.provider.*
import javax.swing.JComponent
import javax.swing.JPanel

class HomeToolWindowTab(
    private val projectServiceProvider: ProjectServiceProvider,
    private val toolWindow: ToolWindow
) : Component, Disposable {
    private val mySplitter = OnePixelSplitter(HomeToolWindowTab::class.java.canonicalName, 0.35f)
    private val myCollectionPanel = ProviderCollection(projectServiceProvider)
    private val myDetailPanels = mutableMapOf<String, ProviderDetailsUI>()
    private val myContents = mutableMapOf<String, Content>()
    private val myMRToolWindowTabs = mutableMapOf<String, MergeRequestToolWindowTab>()
    private val myProjectNotifier = object : ProjectNotifierAdapter() {
        override fun startCodeReview(reviewContext: ReviewContext) {
            myDetailPanels.forEach { it.value.hide() }
            myContents.forEach {
                if (it.key == reviewContext.providerData.id) {
                    it.value.isCloseable = false
                }
            }
        }

        override fun stopCodeReview(reviewContext: ReviewContext) {
            myDetailPanels.forEach { it.value.show() }
            myContents.forEach {
                if (it.key == reviewContext.providerData.id) {
                    it.value.isCloseable = true
                }
            }
        }
    }
    private val myConnection = projectServiceProvider.messageBus.connect()

    private val myDetailsListEventListener = object : MergeRequestCollectionEventListener {
        override fun mergeRequestUnselected() {
        }

        override fun mergeRequestSelected(providerData: ProviderData, mergeRequestInfo: MergeRequestInfo) {
            openTab(providerData, mergeRequestInfo)
        }
    }
    private val myCollectionPanelListEventListener = object : ProviderCollectionListEventListener {
        override fun providerUnselected() {
            mySplitter.secondComponent = null
        }

        override fun providerSelected(providerData: ProviderData) {
            if (null === myDetailPanels[providerData.id]) {
                val details = ProviderDetails(
                    projectServiceProvider,
                    toolWindow,
                    myCollectionPanel.getListEventDispatcher(), providerData
                )
                myDetailPanels[providerData.id] = details
                details.listEventDispatcher.addListener(myDetailsListEventListener)
            }
            mySplitter.secondComponent = myDetailPanels[providerData.id]!!.createComponent()
        }

        override fun providerOpened(providerData: ProviderData) {
            openTab(providerData)
        }
    }
    private val myContentManagerListener = object: ContentManagerListener {
        override fun contentAdded(event: ContentManagerEvent) {
        }

        override fun contentRemoveQuery(event: ContentManagerEvent) {
        }

        override fun selectionChanged(event: ContentManagerEvent) {
        }

        override fun contentRemoved(event: ContentManagerEvent) {
            var removedId: String? = null
            for (entry in myContents) {
                if (entry.value === event.content) {
                    removedId = entry.key
                    break
                }
            }
            if (null !== removedId) {
                myContents.remove(removedId)
            }
        }
    }
    private val myCollectionPanelToolbarEventListener = object: ProviderCollectionToolbarEventListener {
        override fun refreshClicked() {
            myContents.forEach {
                toolWindow.contentManager.removeContent(it.value, true)
            }
            myMRToolWindowTabs.clear()
            myContents.clear()
        }

        override fun helpClicked() {
        }
    }

    init {
        mySplitter.firstComponent = myCollectionPanel.createComponent()
        mySplitter.secondComponent = JPanel()
        toolWindow.contentManager.addContentManagerListener(myContentManagerListener)
        myCollectionPanel.addListEventListener(myCollectionPanelListEventListener)
        myCollectionPanel.addToolbarEventListener(myCollectionPanelToolbarEventListener)
        myConnection.subscribe(ProjectNotifier.TOPIC, myProjectNotifier)
        Disposer.register(projectServiceProvider.project, this)
    }

    private fun findNameForTab(providerData: ProviderData): String {
        if (projectServiceProvider.applicationServiceProvider.isLegal(providerData)) {
            return providerData.name
        }
        return if (providerData.name.length < 20) {
            "${providerData.name} · Not legal for private repository · please buy EE version · only 1\$/month"
        } else {
            "Not legal for private repository · please buy EE version · only 1\$/month · ${providerData.name}"
        }
    }

    private fun openTab(providerData: ProviderData, mergeRequestInfo: MergeRequestInfo? = null) {
        if (null === myContents[providerData.id]) {
            val toolWindowTab = MergeRequestToolWindowTab(
                projectServiceProvider, toolWindow.contentManager, providerData
            )
            myMRToolWindowTabs[providerData.id] = toolWindowTab

            val content = toolWindow.contentManager.factory.createContent(
                toolWindowTab.createComponent(),
                findNameForTab(providerData),
                false
            )
            content.isCloseable = true
            myContents[providerData.id] = content
            toolWindow.contentManager.addContent(content)
        }

        val tab = myContents[providerData.id]
        if (null !== tab) {
            toolWindow.contentManager.setSelectedContent(tab)
        }

        if (null !== mergeRequestInfo) {
            val toolWindowTab = myMRToolWindowTabs[providerData.id]
            if (null !== toolWindowTab) {
                toolWindowTab.selectMergeRequest(mergeRequestInfo)
            }
        }
    }

    override fun createComponent(): JComponent = mySplitter

    override fun dispose() {
        myConnection.disconnect()
    }

}