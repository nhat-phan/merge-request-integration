package net.ntworld.mergeRequestIntegrationIde.mergeRequest.comments

import com.intellij.ui.tabs.TabInfo
import net.ntworld.mergeRequest.MergeRequestInfo
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequestIntegrationIde.service.ApplicationService
import net.ntworld.mergeRequestIntegrationIde.service.ProjectService

object CommentsTabFactory {
    fun makeCommentsTabView(): CommentsTabView {
        return CommentsTabViewImpl()
    }

    fun makeCommentsTabModel(
        projectService: ProjectService,
        providerData: ProviderData
    ): CommentsTabModel {
        return CommentsTabModelImpl(projectService, providerData)
    }

    fun makeCommentsTabPresenter(
        applicationService: ApplicationService,
        projectService: ProjectService,
        model: CommentsTabModel,
        view: CommentsTabView
    ): CommentsTabPresenter {
        return CommentsTabPresenterImpl(applicationService, projectService, model, view)
    }
}