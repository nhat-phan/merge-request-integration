package net.ntworld.mergeRequestIntegrationIde.infrastructure

import com.intellij.ide.AppLifecycleListener
import com.intellij.openapi.application.ApplicationManager
import net.ntworld.foundation.Infrastructure
import net.ntworld.foundation.MemorizedInfrastructure
import net.ntworld.mergeRequest.ProjectVisibility
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequest.ProviderInfo
import net.ntworld.mergeRequest.ProviderStatus
import net.ntworld.mergeRequest.api.ApiCredentials
import net.ntworld.mergeRequestIntegration.ApiProviderManager
import net.ntworld.mergeRequestIntegrationIde.IdeInfrastructure
import net.ntworld.mergeRequestIntegrationIde.compatibility.IntellijIdeApi
import net.ntworld.mergeRequestIntegrationIde.compatibility.Version193Adapter
import net.ntworld.mergeRequestIntegrationIde.infrastructure.internal.ProviderSettingsImpl
import net.ntworld.mergeRequestIntegrationIde.infrastructure.internal.ServiceBase
import net.ntworld.mergeRequestIntegrationIde.infrastructure.setting.ApplicationSettingsManager
import net.ntworld.mergeRequestIntegrationIde.infrastructure.setting.ApplicationSettingsManagerImpl
import net.ntworld.mergeRequestIntegrationIde.watcher.WatcherManager
import net.ntworld.mergeRequestIntegrationIde.watcher.WatcherManagerImpl
import org.jdom.Element
import java.net.URL

abstract class AbstractApplicationServiceProvider : ApplicationServiceProvider, ServiceBase() {
    final override val watcherManager: WatcherManager = WatcherManagerImpl()

    private val publicLegalGrantedDomains = listOf(
        "gitlab.com",
        "www.gitlab.com"
    )
    private val legalGrantedDomains = listOf(
        "gitlab.personio-internal.de"
    )
    private val myAppLifecycleListener = object : AppLifecycleListener {
        override fun appClosing() {
            watcherManager.dispose()
        }
    }

    init {
        val connection = ApplicationManager.getApplication().messageBus.connect()
        connection.subscribe(AppLifecycleListener.TOPIC, myAppLifecycleListener)
//        private val myBranchChangeListener = object: BranchChangeListener {
//            override fun branchWillChange(branchName: String) {
//                println("branchWillChange $branchName")
//            }
//
//            override fun branchHasChanged(branchName: String) {
//                println("branchHasChanged $branchName")
//            }
//        }
//        connection.subscribe(BranchChangeListener.VCS_BRANCH_CHANGED, myBranchChangeListener)
    }

    override val infrastructure: Infrastructure = MemorizedInfrastructure(IdeInfrastructure())

    override val intellijIdeApi: IntellijIdeApi = Version193Adapter()

    override val settingsManager: ApplicationSettingsManager = ApplicationSettingsManagerImpl()

    override fun getState(): Element? {
        val element = super.getState()
        if (null === element) {
            return element
        }
        settingsManager.writeTo(element)
        return element
    }

    override fun loadState(state: Element) {
        super.loadState(state)
        val settings = settingsManager.readFrom(state.children)
        ApiProviderManager.updateApiOptions(settings.toApiOptions())
    }

    override fun addProviderConfiguration(id: String, info: ProviderInfo, credentials: ApiCredentials) {
        myProvidersData[id] = ProviderSettingsImpl(
            id = id,
            info = info,
            credentials = encryptCredentials(info, credentials),
            repository = ""
        )
    }

    override fun removeAllProviderConfigurations() {
        myProvidersData.clear()
        this.state
    }

    override fun getProviderConfigurations(): List<ProviderSettings> {
        return myProvidersData.values.map {
            ProviderSettingsImpl(
                id = it.id,
                info = it.info,
                credentials = decryptCredentials(it.info, it.credentials),
                repository = ""
            )
        }
    }

    override fun isLegal(providerData: ProviderData): Boolean {
        if (providerData.status == ProviderStatus.ERROR || providerData.project.url.isEmpty()) {
            return false
        }
        val url = URL(providerData.project.url)
        if (publicLegalGrantedDomains.contains(url.host) &&
            providerData.project.visibility == ProjectVisibility.PUBLIC) {
            return true
        }
        return legalGrantedDomains.contains(url.host)
    }
}