package net.ntworld.mergeRequestIntegrationIde.mergeRequest.comments.tree

import com.intellij.util.EventDispatcher
import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequestIntegrationIde.AbstractModel

class CommentTreeModelImpl : AbstractModel<CommentTreeModel.DataListener>(), CommentTreeModel {
    override val dispatcher = EventDispatcher.create(CommentTreeModel.DataListener::class.java)

    override var comments: List<Comment> = listOf()
        set(value) {
            field = value
            dispatcher.multicaster.onCommentsUpdated()
        }

    override var displayResolvedComments: Boolean = false
        set(value) {
            field = value
            dispatcher.multicaster.onDisplayResolvedCommentsChanged()
        }
}