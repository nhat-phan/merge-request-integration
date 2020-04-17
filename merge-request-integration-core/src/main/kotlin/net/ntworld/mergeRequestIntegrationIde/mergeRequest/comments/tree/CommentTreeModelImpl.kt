package net.ntworld.mergeRequestIntegrationIde.mergeRequest.comments.tree

import com.intellij.util.EventDispatcher
import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequest.MergeRequestInfo
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequestIntegrationIde.AbstractModel
import net.ntworld.mergeRequestIntegrationIde.mergeRequest.Empty

class CommentTreeModelImpl(
    override val providerData: ProviderData
) : AbstractModel<CommentTreeModel.DataListener>(), CommentTreeModel {
    override val dispatcher = EventDispatcher.create(CommentTreeModel.DataListener::class.java)

    override var mergeRequestInfo: MergeRequestInfo = MergeRequestInfo.Empty
        set(value) {
            field = value
            dispatcher.multicaster.onMergeRequestInfoChanged()
        }

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