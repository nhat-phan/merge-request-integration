package net.ntworld.mergeRequestIntegrationIde.infrastructure.api.provider

import com.intellij.util.messages.MessageBus
import com.intellij.openapi.project.Project as IdeaProject
import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequest.MergeRequestInfo
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequestIntegrationIde.infrastructure.api.MergeRequestDataNotifier
import net.ntworld.mergeRequestIntegrationIde.infrastructure.ApplicationService
import net.ntworld.mergeRequestIntegrationIde.task.GetCommentsTask

class MergeRequestDataProvider(
    private val applicationService: ApplicationService,
    private val project: IdeaProject,
    private val messageBus: MessageBus
) : MergeRequestDataNotifier {
    private val getCommentsTaskListener = object : GetCommentsTask.Listener {
        override fun dataReceived(
            providerData: ProviderData, mergeRequestInfo: MergeRequestInfo, comments: List<Comment>
        ) {
            messageBus.syncPublisher(MergeRequestDataNotifier.TOPIC)
                .onCommentsUpdated(providerData, mergeRequestInfo, comments)
        }
    }

    override fun fetchCommentsRequested(providerData: ProviderData, mergeRequestInfo: MergeRequestInfo) {
        val task = GetCommentsTask(
            applicationService,
            project,
            providerData,
            mergeRequestInfo,
            getCommentsTaskListener
        )
        task.start()
    }

    override fun onCommentsUpdated(providerData: ProviderData, mergeRequestInfo: MergeRequestInfo, comments: List<Comment>) {
    }
}