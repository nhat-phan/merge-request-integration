package net.ntworld.mergeRequestIntegrationIde.infrastructure

import com.intellij.notification.NotificationDisplayType
import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationType
import com.intellij.openapi.vcs.BranchChangeListener
import com.intellij.util.messages.MessageBus
import net.ntworld.foundation.Infrastructure
import net.ntworld.foundation.MemorizedInfrastructure
import net.ntworld.foundation.util.UUIDGenerator
import net.ntworld.mergeRequest.MergeRequest
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequest.ProviderInfo
import net.ntworld.mergeRequest.api.ApiCredentials
import net.ntworld.mergeRequestIntegration.DefaultProviderStorage
import net.ntworld.mergeRequestIntegration.ProviderStorage
import net.ntworld.mergeRequestIntegration.provider.MemoryCache
import net.ntworld.mergeRequestIntegration.provider.github.Github
import net.ntworld.mergeRequestIntegration.provider.gitlab.Gitlab
import net.ntworld.mergeRequestIntegrationIde.DEBUG
import net.ntworld.mergeRequestIntegrationIde.IdeInfrastructure
import net.ntworld.mergeRequestIntegrationIde.compatibility.IntellijIdeApi
import net.ntworld.mergeRequestIntegrationIde.infrastructure.internal.ProviderSettingsImpl
import net.ntworld.mergeRequestIntegrationIde.infrastructure.internal.ReviewContextManagerImpl
import net.ntworld.mergeRequestIntegrationIde.infrastructure.internal.ServiceBase
import net.ntworld.mergeRequestIntegrationIde.infrastructure.notifier.MergeRequestDataNotifier
import net.ntworld.mergeRequestIntegrationIde.infrastructure.notifier.ProjectNotifier
import net.ntworld.mergeRequestIntegrationIde.infrastructure.notifier.provider.MergeRequestDataProvider
import net.ntworld.mergeRequestIntegrationIde.infrastructure.service.FiltersStorageService
import net.ntworld.mergeRequestIntegrationIde.infrastructure.service.RepositoryFileService
import net.ntworld.mergeRequestIntegrationIde.infrastructure.service.internal.FiltersStorageServiceImpl
import net.ntworld.mergeRequestIntegrationIde.infrastructure.service.repositoryFile.CachedRepositoryFile
import net.ntworld.mergeRequestIntegrationIde.infrastructure.service.repositoryFile.LocalRepositoryFileService
import net.ntworld.mergeRequestIntegrationIde.infrastructure.setting.ApplicationSettings
import net.ntworld.mergeRequestIntegrationIde.rework.ReworkManager
import net.ntworld.mergeRequestIntegrationIde.rework.internal.ReworkManagerImpl
import net.ntworld.mergeRequestIntegrationIde.task.RegisterProviderTask
import net.ntworld.mergeRequestIntegrationIde.ui.configuration.GithubConnectionsConfigurableBase
import net.ntworld.mergeRequestIntegrationIde.ui.configuration.GitlabConnectionsConfigurableBase
import org.jdom.Element
import com.intellij.openapi.project.Project as IdeaProject

abstract class AbstractProjectServiceProvider(
    final override val project: IdeaProject
) : ProjectServiceProvider, ServiceBase() {
    private val myNotification: NotificationGroup = NotificationGroup(
        "Merge Request Integration", NotificationDisplayType.BALLOON, true
    )

    final override val providerStorage: ProviderStorage = DefaultProviderStorage()

    override val infrastructure: Infrastructure = MemorizedInfrastructure(IdeInfrastructure(providerStorage))

    override val applicationSettings: ApplicationSettings
        get() = applicationServiceProvider.settingsManager

    override val intellijIdeApi: IntellijIdeApi
        get() = applicationServiceProvider.intellijIdeApi

    final override val messageBus: MessageBus by lazy { project.messageBus }

    override val repositoryFile: RepositoryFileService by lazy {
        CachedRepositoryFile(
            LocalRepositoryFileService(ideaProject = project),
            MemoryCache()
        )
    }

    override val filtersStorage: FiltersStorageService = FiltersStorageServiceImpl(this)

    final override val reviewContextManager: ReviewContextManager = ReviewContextManagerImpl(project)

    private val myPublisher = messageBus.syncPublisher(ProjectNotifier.TOPIC)

    final override val reworkManager: ReworkManager = ReworkManagerImpl(this)

    private val myBranchChangeListener = object: BranchChangeListener {
        override fun branchWillChange(branchName: String) {
        }

        override fun branchHasChanged(branchName: String) {
            if (DEBUG) println("BranchChangeListener triggered, request create ReworkWatcher for $branchName")
            reworkManager.requestCreateReworkWatcher(providerStorage.registeredProviders, branchName)
        }
    }

    protected fun initWithApplicationServiceProvider(applicationSP: ApplicationServiceProvider) {
        applicationSP.watcherManager.addWatcher(reviewContextManager)

        val connection = messageBus.connect(project)
        connection.subscribe(MergeRequestDataNotifier.TOPIC, MergeRequestDataProvider(this, messageBus))
        connection.subscribe(BranchChangeListener.VCS_BRANCH_CHANGED, myBranchChangeListener)
    }

    override fun readStateItem(item: Element, id: String, settings: ProviderSettings) {
        super.readStateItem(item, id, settings)
        filtersStorage.readFrom(item, id)
    }

    override fun writeStateItem(item: Element, id: String, settings: ProviderSettings) {
        super.writeStateItem(item, id, settings)
        filtersStorage.writeTo(item, id)
    }

    override fun addProviderConfiguration(
        id: String,
        info: ProviderInfo,
        credentials: ApiCredentials,
        repository: String
    ) {
        providerSettingsData[id] = ProviderSettingsImpl(
            id = id,
            info = info,
            credentials = encryptCredentials(info, credentials),
            repository = repository
        )
    }

    override fun removeProviderConfiguration(id: String) {
        providerSettingsData.remove(id)
    }

    override fun getProviderConfigurations(): List<ProviderSettings> {
        return providerSettingsData.values.map {
            ProviderSettingsImpl(
                id = it.id,
                info = it.info,
                credentials = decryptCredentials(it.info, it.credentials),
                repository = it.repository
            )
        }
    }

    override fun initialize() {
        providerStorage.clear()
        reworkManager.clearAllBranchWatchers()
        myPublisher.starting()

        getProviderConfigurations().forEach { registerProviderSettings(it) }
        myPublisher.initialized()
    }

    override fun isDoingCodeReview(): Boolean = null !== reviewContextManager.findDoingCodeReviewContext()

    override fun startCodeReview(providerData: ProviderData, mergeRequest: MergeRequest) {
        reviewContextManager.setContextToDoingCodeReview(providerData.id, mergeRequest.id)
        val reviewContext = reviewContextManager.findDoingCodeReviewContext()
        if (null !== reviewContext) {
            myPublisher.startCodeReview(reviewContext)
        }
    }

    override fun stopCodeReview() {
        val reviewContext = reviewContextManager.findDoingCodeReviewContext()
        if (null !== reviewContext) {
            myPublisher.stopCodeReview(reviewContext)
            reviewContext.closeAllChanges()
        }
        reviewContextManager.clearContextDoingCodeReview()
    }

    override fun notify(message: String) {
        notify(message, NotificationType.INFORMATION)
    }

    override fun notify(message: String, type: NotificationType) {
        val notification = myNotification.createNotification(message, type)
        notification.notify(project)
    }

    private fun registerProviderSettings(settings: ProviderSettings) {
        var name = ""
        if (settings.info.id == Gitlab.id) {
            name = GitlabConnectionsConfigurableBase.findNameFromId(settings.id)
        }
        if (settings.info.id == Github.id) {
            name = GithubConnectionsConfigurableBase.findNameFromId(settings.id)
        }

        val task = RegisterProviderTask(
            this,
            id = UUIDGenerator.generate(),
            name = name,
            settings = settings,
            listener = object : RegisterProviderTask.Listener {
                override fun providerRegistered(providerData: ProviderData) {
                    reworkManager.createBranchWatcher(providerData)
                    messageBus.syncPublisher(ProjectNotifier.TOPIC).providerRegistered(providerData)
                }
            }
        )
        task.start()
    }
}