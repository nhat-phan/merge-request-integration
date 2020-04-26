package net.ntworld.mergeRequestIntegrationIde.mergeRequest.comments.tree

import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequestIntegrationIde.infrastructure.ProjectServiceProvider

object CommentTreeFactory {
    fun makeModel(providerData: ProviderData): CommentTreeModel {
        return CommentTreeModelImpl(providerData)
    }

    fun makeView(projectServiceProvider: ProjectServiceProvider, providerData: ProviderData): CommentTreeView {
        return CommentTreeViewImpl(projectServiceProvider, providerData)
    }

    fun makePresenter(
        model: CommentTreeModel,
        view: CommentTreeView
    ): CommentTreePresenter {
        return CommentTreePresenterImpl(model, view)
    }
}
