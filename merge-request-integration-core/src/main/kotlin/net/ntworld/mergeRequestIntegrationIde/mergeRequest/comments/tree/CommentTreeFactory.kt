package net.ntworld.mergeRequestIntegrationIde.mergeRequest.comments.tree

import com.intellij.openapi.project.Project as IdeaProject

object CommentTreeFactory {
    fun makeModel(): CommentTreeModel {
        return CommentTreeModelImpl()
    }

    fun makeView(ideaProject: IdeaProject): CommentTreeView {
        return CommentTreeViewImpl(ideaProject)
    }

    fun makePresenter(
        model: CommentTreeModel,
        view: CommentTreeView
    ): CommentTreePresenter {
        return CommentTreePresenterImpl(model, view)
    }
}
