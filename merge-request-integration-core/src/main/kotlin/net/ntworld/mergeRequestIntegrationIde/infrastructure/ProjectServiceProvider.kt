package net.ntworld.mergeRequestIntegrationIde.infrastructure

import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationType
import com.intellij.util.EventDispatcher
import com.intellij.util.messages.MessageBus
import net.ntworld.foundation.Infrastructure
import net.ntworld.mergeRequest.*
import net.ntworld.mergeRequest.api.ApiCredentials
import net.ntworld.mergeRequest.api.MergeRequestOrdering
import net.ntworld.mergeRequest.query.GetMergeRequestFilter
import net.ntworld.mergeRequestIntegrationIde.compatibility.IntellijIdeApi
import net.ntworld.mergeRequestIntegrationIde.infrastructure.service.RepositoryFileService
import net.ntworld.mergeRequestIntegrationIde.infrastructure.setting.ApplicationSettings
import com.intellij.openapi.project.Project as IdeaProject

interface ProjectServiceProvider {
    val applicationServiceProvider: ApplicationServiceProvider

    val project: IdeaProject

    val messageBus: MessageBus

    val dispatcher: EventDispatcher<ProjectEventListener>

    val notification: NotificationGroup

    val registeredProviders: List<ProviderData>

    val repositoryFile: RepositoryFileService

    val applicationSettings: ApplicationSettings

    val infrastructure: Infrastructure

    val intellijIdeApi: IntellijIdeApi

    val reviewContextManager: ReviewContextManager

    fun findFiltersByProviderId(id: String): Pair<GetMergeRequestFilter, MergeRequestOrdering>

    fun saveFiltersOfProvider(id: String, filters: GetMergeRequestFilter, ordering: MergeRequestOrdering)

    fun supported(): List<ProviderInfo>

    fun addProviderConfiguration(id: String, info: ProviderInfo, credentials: ApiCredentials, repository: String)

    fun removeProviderConfiguration(id: String)

    fun getProviderConfigurations(): List<ProviderSettings>

    fun clear()

    fun register(settings: ProviderSettings)

    fun isDoingCodeReview(): Boolean

    fun notify(message: String)

    fun notify(message: String, type: NotificationType)
}
