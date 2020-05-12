package net.ntworld.mergeRequestIntegrationIde.mergeRequest.comments.tree

import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequestIntegrationIde.infrastructure.ProjectServiceProvider

object CommentTreeFactory {
    fun makeModel(providerData: ProviderData): CommentTreeModel {
        return CommentTreeModelImpl(providerData)
    }

    fun makeView(
        projectServiceProvider: ProjectServiceProvider,
        providerData: ProviderData,
        showOpenDiffViewDescription: Boolean
    ): CommentTreeView {
        return CommentTreeViewImpl(projectServiceProvider, providerData, showOpenDiffViewDescription)
    }

    fun makePresenter(
        model: CommentTreeModel,
        view: CommentTreeView
    ): CommentTreePresenter {
        return CommentTreePresenterImpl(model, view)
    }
}
