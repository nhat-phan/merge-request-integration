package net.ntworld.mergeRequestIntegrationIde.task

import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.progress.Task
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequestIntegration.ApiProviderManager
import net.ntworld.mergeRequestIntegrationIde.infrastructure.ApplicationServiceProvider
import net.ntworld.mergeRequestIntegrationIde.infrastructure.ProviderSettings

class RegisterProviderTask(
    private val applicationServiceProvider: ApplicationServiceProvider,
    ideaProject: Project,
    private val id: String,
    private val name: String,
    private val settings: ProviderSettings,
    private val listener: Listener
) : Task.Backgroundable(ideaProject, "Fetching provider information...", false) {

    fun start() {
        ProgressManager.getInstance().runProcessWithProgressAsynchronously(
            this,
            Indicator(this)
        )
    }

    override fun run(indicator: ProgressIndicator) {
        try {
            val providerData = ApiProviderManager.register(
                infrastructure = applicationServiceProvider.infrastructure,
                id = id,
                key = settings.id,
                name = name,
                info = settings.info,
                credentials = settings.credentials,
                repository = settings.repository
            )
            listener.providerRegistered(providerData)
        } catch (exception: Exception) {
            listener.onError(exception)
        }
    }

    private class Indicator(private val task: RegisterProviderTask) : BackgroundableProcessIndicator(task)

    interface Listener {
        fun onError(exception: Exception)

        fun providerRegistered(providerData: ProviderData)
    }
}