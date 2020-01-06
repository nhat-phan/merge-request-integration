package net.ntworld.mergeRequestIntegrationIde.ui.provider

import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.wm.ToolWindow
import net.ntworld.mergeRequest.CommentPosition
import net.ntworld.mergeRequest.MergeRequest
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequestIntegrationIde.service.CommentStore
import net.ntworld.mergeRequestIntegrationIde.service.ProjectEventListener
import net.ntworld.mergeRequestIntegrationIde.service.ProjectService
import net.ntworld.mergeRequestIntegrationIde.ui.Component
import javax.swing.JComponent
import com.intellij.openapi.project.Project as IdeaProject

class ProviderCollection(
    private val ideaProject: IdeaProject,
    private val toolWindow: ToolWindow
): Component {
    private val myProjectService = ProjectService.getInstance(ideaProject)
    private val myListUI: ProviderCollectionListUI by lazy {
        val list = ProviderCollectionList()
        list.setProviders(myProjectService.registeredProviders)
        list
    }
    private val myToolbarUI: ProviderCollectionToolbarUI by lazy {
        ProviderCollectionToolbar()
    }
    private val myProjectEventListener = object: ProjectEventListener {
        override fun providersClear() {
            myListUI.clear()
        }

        override fun providerRegistered(providerData: ProviderData) {
            myListUI.addProvider(providerData)
        }

        override fun startCodeReview(providerData: ProviderData, mergeRequest: MergeRequest) {
            myComponent.isVisible = false
        }

        override fun stopCodeReview(providerData: ProviderData, mergeRequest: MergeRequest) {
            myComponent.isVisible = true
        }

    }
    private val myToolbarEventListener = object: ProviderCollectionToolbarEventListener {
        override fun addClicked() {
        }

        override fun refreshClicked() {
            myProjectService.clear()
            myListUI.setProviders(myProjectService.registeredProviders)
        }

        override fun helpClicked() {
        }
    }

    private val myComponent = SimpleToolWindowPanel(true, true)

    init {
        myComponent.setContent(myListUI.createComponent())
        myComponent.toolbar = myToolbarUI.createComponent()

        myToolbarUI.eventDispatcher.addListener(myToolbarEventListener)
        myProjectService.dispatcher.addListener(myProjectEventListener)
    }

    override fun createComponent(): JComponent = myComponent

    fun getListEventDispatcher() = myListUI.eventDispatcher

    fun addListEventListener(listener: ProviderCollectionListEventListener) {
        myListUI.eventDispatcher.addListener(listener)
    }

    fun addToolbarEventListener(listener: ProviderCollectionToolbarEventListener) {
        myToolbarUI.eventDispatcher.addListener(listener)
    }
}