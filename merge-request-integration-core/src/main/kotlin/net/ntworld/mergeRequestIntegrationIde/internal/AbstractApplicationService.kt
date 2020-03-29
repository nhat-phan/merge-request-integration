package net.ntworld.mergeRequestIntegrationIde.internal

import com.intellij.ide.AppLifecycleListener
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.BranchChangeListener
import com.intellij.util.messages.MessageBus
import net.ntworld.foundation.Infrastructure
import net.ntworld.foundation.MemorizedInfrastructure
import net.ntworld.mergeRequest.ProjectVisibility
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequest.ProviderInfo
import net.ntworld.mergeRequest.ProviderStatus
import net.ntworld.mergeRequest.api.ApiCredentials
import net.ntworld.mergeRequestIntegration.ApiProviderManager
import net.ntworld.mergeRequestIntegrationIde.IdeInfrastructure
import net.ntworld.mergeRequestIntegrationIde.compatibility.*
import net.ntworld.mergeRequestIntegrationIde.internal.option.*
import net.ntworld.mergeRequestIntegrationIde.service.ApplicationService
import net.ntworld.mergeRequestIntegrationIde.service.ApplicationSettings
import net.ntworld.mergeRequestIntegrationIde.service.ProviderSettings
import net.ntworld.mergeRequestIntegrationIde.watcher.WatcherManager
import net.ntworld.mergeRequestIntegrationIde.watcher.WatcherManagerImpl
import org.jdom.Element
import java.net.URL

abstract class AbstractApplicationService : ApplicationService, ServiceBase() {
    override val watcherManager: WatcherManager = WatcherManagerImpl()

    private val publicLegalGrantedDomains = listOf(
        "gitlab.com",
        "www.gitlab.com"
    )
    private val legalGrantedDomains = listOf(
        "gitlab.personio-internal.de"
    )
    private val myOptionEnableRequestCache = EnableRequestCacheOption()
    private val myOptionSaveMRFilterState = SaveMRFilterStateOption()
    private val myOptionGroupCommentsByThread = GroupCommentsByThreadOption()
    private val myOptionDisplayCommentsInDiffView = DisplayCommentsInDiffViewOption()
    private val myOptionShowAddCommentIconsInDiffViewGutter = ShowAddCommentIconsInDiffViewGutterOption()
    private val myOptionCheckoutTargetBranch = CheckoutTargetBranchOption()
    private val myOptionMaxDiffChangesOpenedAutomatically = MaxDiffChangesOpenedAutomaticallyOption()
    private val myAllSettingOptions = listOf<SettingOption<*>>(
        myOptionEnableRequestCache,
        myOptionSaveMRFilterState,
        myOptionGroupCommentsByThread,
        myOptionDisplayCommentsInDiffView,
        myOptionShowAddCommentIconsInDiffViewGutter,
        myOptionCheckoutTargetBranch,
        myOptionMaxDiffChangesOpenedAutomatically
    )
    private var myApplicationSettings : ApplicationSettings = ApplicationSettingsImpl.DEFAULT
    private val myAppLifecycleListener = object : AppLifecycleListener {
        override fun appClosing() {
            watcherManager.dispose()
        }

        override fun appStarting(projectFromCommandLine: Project?) {
            println(projectFromCommandLine)
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

    override fun supported(): List<ProviderInfo> = supportedProviders

    override val infrastructure: Infrastructure = MemorizedInfrastructure(IdeInfrastructure())

    override val intellijIdeApi: IntellijIdeApi = Version201Adapter()

    override val settings: ApplicationSettings
        get() = myApplicationSettings

    final override val messageBus: MessageBus = ApplicationManager.getApplication().messageBus

    override fun getState(): Element? {
        val element = super.getState()
        if (null === element) {
            return element
        }
        writeSettingOption(element, myOptionEnableRequestCache, myApplicationSettings.enableRequestCache)
        writeSettingOption(element, myOptionSaveMRFilterState, myApplicationSettings.saveMRFilterState)
        writeSettingOption(element, myOptionGroupCommentsByThread, myApplicationSettings.groupCommentsByThread)
        writeSettingOption(element, myOptionDisplayCommentsInDiffView, myApplicationSettings.displayCommentsInDiffView)
        writeSettingOption(element, myOptionShowAddCommentIconsInDiffViewGutter, myApplicationSettings.showAddCommentIconsInDiffViewGutter)
        writeSettingOption(element, myOptionCheckoutTargetBranch, myApplicationSettings.checkoutTargetBranch)
        writeSettingOption(element, myOptionMaxDiffChangesOpenedAutomatically, myApplicationSettings.maxDiffChangesOpenedAutomatically)
        return element
    }

    override fun loadState(state: Element) {
        super.loadState(state)
        var settings = ApplicationSettingsImpl.DEFAULT
        for (item in state.children) {
            if (item.name != "Setting") {
                continue
            }

            val nameAttribute = item.getAttribute("name")
            if (null === nameAttribute) {
                continue
            }

            val valueAttribute = item.getAttribute("value")
            if (null === valueAttribute) {
                continue
            }

            for (option in myAllSettingOptions) {
                if (option.name == nameAttribute.value.trim()) {
                    settings = option.readValue(valueAttribute.value, settings)
                }
            }
        }
        myApplicationSettings = settings
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

    override fun updateSettings(settings: ApplicationSettings) {
        myApplicationSettings = settings
    }

    private fun<T> writeSettingOption(root: Element, option: SettingOption<T>, value: T) {
        val item = Element("Setting")
        item.setAttribute("name", option.name)
        item.setAttribute("value", option.writeValue(value))
        root.addContent(item)
    }
}