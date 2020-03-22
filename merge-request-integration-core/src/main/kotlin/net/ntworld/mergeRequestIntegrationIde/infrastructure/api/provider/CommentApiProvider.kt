package net.ntworld.mergeRequestIntegrationIde.infrastructure.api.provider

import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequest.MergeRequest
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequestIntegrationIde.infrastructure.api.CommentApiObserver
import net.ntworld.mergeRequestIntegrationIde.service.ProjectService
import net.ntworld.mergeRequestIntegrationIde.task.GetCommentsTask

class CommentApiProvider(
    private val projectService: ProjectService
) : CommentApiObserver {
    private val getCommentsTaskListener = object : GetCommentsTask.Listener {
        override fun dataReceived(providerData: ProviderData, mergeRequest: MergeRequest, comments: List<Comment>) {
            projectService.messageBus.syncPublisher(CommentApiObserver.TOPIC)
                .onCommentsUpdated(providerData, mergeRequest, comments)
        }
    }

    override fun fetchCommentsRequested(providerData: ProviderData, mergeRequest: MergeRequest) {
        val task = GetCommentsTask(
            projectService.getApplicationService(),
            projectService.project,
            providerData,
            mergeRequest,
            getCommentsTaskListener
        )
        task.start()
    }

    override fun onCommentsUpdated(providerData: ProviderData, mergeRequest: MergeRequest, comments: List<Comment>) {
    }
}