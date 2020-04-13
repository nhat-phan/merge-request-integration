package net.ntworld.mergeRequestIntegrationIde.mergeRequest.comments.tree

import com.intellij.util.EventDispatcher
import net.ntworld.mergeRequestIntegrationIde.AbstractPresenter
import java.util.*

class CommentTreePresenterImpl(
    override val model: CommentTreeModel,
    override val view: CommentTreeView
) : AbstractPresenter<EventListener>(), CommentTreePresenter, CommentTreeModel.DataListener {
    override val dispatcher = EventDispatcher.create(EventListener::class.java)

    init {
        model.addDataListener(this)
        // view.addActionListener()
    }

    override fun onCommentsUpdated() {
        view.renderTree(model.comments)
    }

    override fun onDisplayResolvedCommentsChanged() {
        view.setShowResolvedCommentState(model.displayResolvedComments)
    }

}