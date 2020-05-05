package net.ntworld.mergeRequestIntegrationIde.toolWindow.internal

import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequest.MergeRequestInfo
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequestIntegrationIde.infrastructure.ProjectServiceProvider
import net.ntworld.mergeRequestIntegrationIde.toolWindow.CommentsToolWindowTab
import javax.swing.JComponent
import javax.swing.JLabel

class CommentsToolWindowTabImpl(
    private val projectServiceProvider: ProjectServiceProvider,
    override val providerData: ProviderData,
    mergeRequestInfo: MergeRequestInfo,
    comments: List<Comment>
): CommentsToolWindowTab {
    // private val myModel

    override fun setMergeRequestInfo(mergeRequestInfo: MergeRequestInfo) {
    }

    override fun setComments(comments: List<Comment>) {
    }

    override fun setDisplayResolvedComments(value: Boolean) {
    }

    override val component: JComponent = JLabel()
}