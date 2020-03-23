package net.ntworld.mergeRequestIntegrationIde.infrastructure.api.provider

import com.intellij.util.messages.MessageBus
import com.intellij.openapi.project.Project as IdeaProject
import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequest.MergeRequest
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequestIntegrationIde.infrastructure.api.MergeRequestDataNotifier
import net.ntworld.mergeRequestIntegrationIde.service.ApplicationService
import net.ntworld.mergeRequestIntegrationIde.task.GetCommentsTask

class MergeRequestDataProvider(
    private val applicationService: ApplicationService,
    private val project: IdeaProject,
    private val messageBus: MessageBus
) : MergeRequestDataNotifier {
    private val getCommentsTaskListener = object : GetCommentsTask.Listener {
        override fun dataReceived(providerData: ProviderData, mergeRequest: MergeRequest, comments: List<Comment>) {
            messageBus.syncPublisher(MergeRequestDataNotifier.TOPIC)
                .onCommentsUpdated(providerData, mergeRequest, comments)
        }
    }

    override fun fetchCommentsRequested(providerData: ProviderData, mergeRequest: MergeRequest) {
        val task = GetCommentsTask(
            applicationService,
            project,
            providerData,
            mergeRequest,
            getCommentsTaskListener
        )
        task.start()
    }

    override fun onCommentsUpdated(providerData: ProviderData, mergeRequest: MergeRequest, comments: List<Comment>) {
    }
}