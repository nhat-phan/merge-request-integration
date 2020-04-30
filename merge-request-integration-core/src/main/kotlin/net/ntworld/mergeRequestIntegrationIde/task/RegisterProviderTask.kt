package net.ntworld.mergeRequestIntegrationIde.task

import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequestIntegrationIde.infrastructure.ProjectServiceProvider
import net.ntworld.mergeRequestIntegrationIde.infrastructure.ProviderSettings

class RegisterProviderTask(
    private val projectServiceProvider: ProjectServiceProvider,
    private val id: String,
    private val name: String,
    private val settings: ProviderSettings,
    private val listener: Listener
) : Task.Backgroundable(projectServiceProvider.project, "Fetching provider information...", false) {

    fun start() {
        ProgressManager.getInstance().runProcessWithProgressAsynchronously(
            this,
            Indicator(this)
        )
    }

    override fun run(indicator: ProgressIndicator) {
        try {
            val providerData = projectServiceProvider.providerStorage.register(
                infrastructure = projectServiceProvider.infrastructure,
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
        fun onError(exception: Exception) {
            throw exception
        }

        fun providerRegistered(providerData: ProviderData)
    }
}