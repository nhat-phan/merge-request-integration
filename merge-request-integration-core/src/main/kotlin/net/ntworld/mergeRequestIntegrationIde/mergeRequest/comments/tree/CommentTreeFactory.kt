package net.ntworld.mergeRequestIntegrationIde.mergeRequest.comments.tree

import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequestIntegrationIde.service.ProjectService

object CommentTreeFactory {
    fun makeModel(providerData: ProviderData): CommentTreeModel {
        return CommentTreeModelImpl(providerData)
    }

    fun makeView(projectService: ProjectService, providerData: ProviderData): CommentTreeView {
        return CommentTreeViewImpl(projectService, providerData)
    }

    fun makePresenter(
        model: CommentTreeModel,
        view: CommentTreeView
    ): CommentTreePresenter {
        return CommentTreePresenterImpl(model, view)
    }
}
