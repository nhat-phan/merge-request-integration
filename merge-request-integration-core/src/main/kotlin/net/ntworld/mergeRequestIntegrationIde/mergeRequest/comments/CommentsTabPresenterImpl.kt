package net.ntworld.mergeRequestIntegrationIde.mergeRequest.comments

import com.intellij.openapi.application.ApplicationManager
import com.intellij.util.EventDispatcher
import net.ntworld.mergeRequestIntegrationIde.AbstractPresenter
import net.ntworld.mergeRequestIntegrationIde.DataChangedSource
import net.ntworld.mergeRequestIntegrationIde.infrastructure.api.MergeRequestDataNotifier
import net.ntworld.mergeRequestIntegrationIde.mergeRequest.isEmpty
import net.ntworld.mergeRequestIntegrationIde.service.ApplicationService
import net.ntworld.mergeRequestIntegrationIde.service.ProjectService
import java.util.*

class CommentsTabPresenterImpl(
    private val applicationService: ApplicationService,
    private val projectService: ProjectService,
    override val model: CommentsTabModel,
    override val view: CommentsTabView
) : AbstractPresenter<EventListener>(), CommentsTabPresenter, CommentsTabModel.DataListener {
    override val dispatcher = EventDispatcher.create(EventListener::class.java)

    init {
        model.addDataListener(this)
    }

    override fun onMergeRequestInfoChanged() {
        val mergeRequestInfo = model.mergeRequestInfo
        if (!mergeRequestInfo.isEmpty()) {
            projectService.messageBus.syncPublisher(MergeRequestDataNotifier.TOPIC).fetchCommentsRequested(
                model.providerData, mergeRequestInfo
            )
        }
    }

    override fun onCommentsUpdated(source: DataChangedSource) {
        if (source == DataChangedSource.NOTIFIER) {
            ApplicationManager.getApplication().invokeLater {
                handleWhenCommentsGetUpdated(source)
            }
        } else {
            handleWhenCommentsGetUpdated(source)
        }

    }

    override fun dispose() {
        view.dispose()
        model.dispose()
    }

    private fun handleWhenCommentsGetUpdated(source: DataChangedSource) {
        view.displayCommentCount(model.comments.size)
        view.renderTree(model.comments, model.displayResolvedComments)
    }
}