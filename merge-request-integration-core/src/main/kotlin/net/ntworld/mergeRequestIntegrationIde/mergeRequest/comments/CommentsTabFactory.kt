package net.ntworld.mergeRequestIntegrationIde.mergeRequest.comments

import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequestIntegrationIde.infrastructure.ApplicationServiceProvider
import net.ntworld.mergeRequestIntegrationIde.infrastructure.ProjectServiceProvider

object CommentsTabFactory {
    fun makeCommentsTabView(
        projectServiceProvider: ProjectServiceProvider,
        providerData: ProviderData
    ): CommentsTabView {
        return CommentsTabViewImpl(projectServiceProvider, providerData)
    }

    fun makeCommentsTabModel(
        projectServiceProvider: ProjectServiceProvider,
        providerData: ProviderData
    ): CommentsTabModel {
        return CommentsTabModelImpl(projectServiceProvider, providerData)
    }

    fun makeCommentsTabPresenter(
        projectServiceProvider: ProjectServiceProvider,
        model: CommentsTabModel,
        view: CommentsTabView
    ): CommentsTabPresenter {
        return CommentsTabPresenterImpl(projectServiceProvider, model, view)
    }
}