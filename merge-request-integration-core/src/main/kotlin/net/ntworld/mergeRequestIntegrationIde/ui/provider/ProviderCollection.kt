package net.ntworld.mergeRequestIntegrationIde.ui.provider

import com.intellij.openapi.Disposable
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.util.Disposer
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequestIntegrationIde.infrastructure.ProjectServiceProvider
import net.ntworld.mergeRequestIntegrationIde.infrastructure.ReviewContext
import net.ntworld.mergeRequestIntegrationIde.infrastructure.notifier.ProjectNotifier
import net.ntworld.mergeRequestIntegrationIde.infrastructure.notifier.ProjectNotifierAdapter
import net.ntworld.mergeRequestIntegrationIde.ui.Component
import javax.swing.JComponent

class ProviderCollection(
    private val projectServiceProvider: ProjectServiceProvider
): Component, Disposable {
    private val myListUI: ProviderCollectionListUI by lazy {
        val list = ProviderCollectionList()
        val registered = projectServiceProvider.providerStorage.registeredProviders
        if (registered.isNotEmpty()) {
            registered.forEach { list.addProvider(it) }
        }
        list
    }
    private val myToolbarUI: ProviderCollectionToolbarUI by lazy {
        ProviderCollectionToolbar()
    }

    private val myProjectNotifier = object : ProjectNotifierAdapter() {
        override fun providerRegistered(providerData: ProviderData) {
            myListUI.addProvider(providerData)
        }

        override fun startCodeReview(reviewContext: ReviewContext) {
            myComponent.isVisible = false
        }

        override fun stopCodeReview(reviewContext: ReviewContext) {
            myComponent.isVisible = true
        }
    }
    private val myConnection = projectServiceProvider.messageBus.connect()
    private val myToolbarEventListener = object: ProviderCollectionToolbarEventListener {
        override fun refreshClicked() {
            myListUI.clear()
            projectServiceProvider.initialize()
        }

        override fun helpClicked() {
        }
    }

    private val myComponent = SimpleToolWindowPanel(true, true)

    init {
        myConnection.subscribe(ProjectNotifier.TOPIC, myProjectNotifier)
        if (!projectServiceProvider.isInitialized()) {
            projectServiceProvider.initialize()
        }

        myComponent.setContent(myListUI.createComponent())
        myComponent.toolbar = myToolbarUI.createComponent()

        myToolbarUI.eventDispatcher.addListener(myToolbarEventListener)
        Disposer.register(projectServiceProvider.project, this)
    }

    override fun createComponent(): JComponent = myComponent

    fun getListEventDispatcher() = myListUI.eventDispatcher

    fun addListEventListener(listener: ProviderCollectionListEventListener) {
        myListUI.eventDispatcher.addListener(listener)
    }

    fun addToolbarEventListener(listener: ProviderCollectionToolbarEventListener) {
        myToolbarUI.eventDispatcher.addListener(listener)
    }

    override fun dispose() {
        myConnection.disconnect()
        myListUI.clear()
    }
}