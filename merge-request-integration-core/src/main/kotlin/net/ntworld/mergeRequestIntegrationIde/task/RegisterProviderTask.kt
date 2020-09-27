package net.ntworld.mergeRequestIntegrationIde.task

import com.intellij.notification.NotificationType
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequest.ProviderStatus
import net.ntworld.mergeRequestIntegrationIde.infrastructure.ProjectServiceProvider
import net.ntworld.mergeRequestIntegrationIde.infrastructure.ProviderSettings

class RegisterProviderTask(
        private val projectServiceProvider: ProjectServiceProvider,
        private val id: String,
        private val name: String,
        private val settings: ProviderSettings,
        private val listener: Listener
) : Task.Backgroundable(projectServiceProvider.project, "Fetching provider information...", true) {

    fun start() {
        ProgressManager.getInstance().runProcessWithProgressAsynchronously(
                this,
                Indicator(this)
        )
    }

    override fun run(indicator: ProgressIndicator) {
        val job = GlobalScope.launch {
            val pair = projectServiceProvider.providerStorage.register(
                    infrastructure = projectServiceProvider.infrastructure,
                    id = id,
                    key = settings.id,
                    name = name,
                    info = settings.info,
                    credentials = settings.credentials,
                    repository = settings.repository
            )
            if (!indicator.isCanceled) {
                listener.providerRegistered(pair.first)
                if (pair.first.status == ProviderStatus.ERROR) {
                    projectServiceProvider.notify(pair.first.errorMessage ?: "", NotificationType.ERROR)
                }
                if (null !== pair.second) {
                    throw pair.second!!
                }
            }
        }
        job.start()
        while (job.isActive && !indicator.isCanceled) {
            Thread.sleep(100);
        }
        if (job.isActive) {
            job.cancel()
        }
        indicator.checkCanceled()
    }

    private class Indicator(private val task: RegisterProviderTask) : BackgroundableProcessIndicator(task)

    interface Listener {
        fun providerRegistered(providerData: ProviderData)
    }
}