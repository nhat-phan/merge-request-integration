package net.ntworld.mergeRequestIntegrationIde.mergeRequest.comments.tree

object CommentTreeFactory {
    fun makeModel(): CommentTreeModel {
        return CommentTreeModelImpl()
    }

    fun makeView(): CommentTreeView {
        return CommentTreeViewImpl()
    }

    fun makePresenter(
        model: CommentTreeModel,
        view: CommentTreeView
    ): CommentTreePresenter {
        return CommentTreePresenterImpl(model, view)
    }
}
