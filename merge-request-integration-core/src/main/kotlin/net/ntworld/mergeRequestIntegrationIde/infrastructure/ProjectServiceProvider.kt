package net.ntworld.mergeRequestIntegrationIde.infrastructure

import com.intellij.notification.NotificationType
import com.intellij.util.messages.MessageBus
import net.ntworld.foundation.Infrastructure
import net.ntworld.mergeRequest.MergeRequest
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequest.ProviderInfo
import net.ntworld.mergeRequest.api.ApiCredentials
import net.ntworld.mergeRequestIntegration.ProviderStorage
import net.ntworld.mergeRequestIntegrationIde.compatibility.IntellijIdeApi
import net.ntworld.mergeRequestIntegrationIde.infrastructure.notifier.SingleMRToolWindowNotifier
import net.ntworld.mergeRequestIntegrationIde.infrastructure.notifier.ProjectNotifier
import net.ntworld.mergeRequestIntegrationIde.infrastructure.service.FiltersStorageService
import net.ntworld.mergeRequestIntegrationIde.infrastructure.service.RepositoryFileService
import net.ntworld.mergeRequestIntegrationIde.infrastructure.setting.ApplicationSettings
import net.ntworld.mergeRequestIntegrationIde.rework.EditorManager
import net.ntworld.mergeRequestIntegrationIde.rework.ReworkManager
import com.intellij.openapi.project.Project as IdeaProject

interface ProjectServiceProvider {
    val applicationServiceProvider: ApplicationServiceProvider

    val infrastructure: Infrastructure

    val providerStorage: ProviderStorage

    val applicationSettings: ApplicationSettings

    val intellijIdeApi: IntellijIdeApi

    val project: IdeaProject

    val messageBus: MessageBus

    val repositoryFile: RepositoryFileService

    val reworkManager: ReworkManager

    val editorManager: EditorManager

    val filtersStorage: FiltersStorageService

    val reviewContextManager: ReviewContextManager

    val projectNotifierTopic: ProjectNotifier

    val singleMRToolWindowNotifierTopic: SingleMRToolWindowNotifier

    fun addProviderConfiguration(id: String, info: ProviderInfo, credentials: ApiCredentials, repository: String)

    fun removeProviderConfiguration(id: String)

    fun getProviderConfigurations(): List<ProviderSettings>

    fun openSingleMRToolWindow(invoker: (() -> Unit)?)

    fun hideSingleMRToolWindow(invoker: (() -> Unit)?)

    fun initialize()

    fun startCodeReview(providerData: ProviderData, mergeRequest: MergeRequest)

    fun stopCodeReview()

    fun isDoingCodeReview(): Boolean

    fun notify(message: String)

    fun notify(message: String, type: NotificationType)
}
