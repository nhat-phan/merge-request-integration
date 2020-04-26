package net.ntworld.mergeRequestIntegrationIde.mergeRequest.comments

import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequestIntegrationIde.infrastructure.ApplicationService
import net.ntworld.mergeRequestIntegrationIde.infrastructure.ProjectService

object CommentsTabFactory {
    fun makeCommentsTabView(
        projectService: ProjectService,
        providerData: ProviderData
    ): CommentsTabView {
        return CommentsTabViewImpl(projectService, providerData)
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