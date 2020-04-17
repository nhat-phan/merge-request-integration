package net.ntworld.mergeRequestIntegrationIde.mergeRequest.comments.tree

class CommentTreePresenterImpl(
    override val model: CommentTreeModel,
    override val view: CommentTreeView
) : CommentTreePresenter, CommentTreeModel.DataListener {

    init {
        model.addDataListener(this)
    }

    override fun onMergeRequestInfoChanged() {
    }

    override fun onCommentsUpdated() {
        view.renderTree(model.mergeRequestInfo, model.comments)
    }

    override fun onDisplayResolvedCommentsChanged() {
        view.setShowResolvedCommentState(model.displayResolvedComments)
    }

    override fun addListener(listener: CommentTreePresenter.Listener) = view.addActionListener(listener)
    override fun removeListener(listener: CommentTreePresenter.Listener) = view.removeActionListener(listener)
}