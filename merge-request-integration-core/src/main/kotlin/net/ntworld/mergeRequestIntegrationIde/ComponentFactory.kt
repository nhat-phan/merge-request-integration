package net.ntworld.mergeRequestIntegrationIde

import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequest.MergeRequestInfo
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequestIntegrationIde.component.PaginationToolbar
import net.ntworld.mergeRequestIntegrationIde.component.comment.CommentComponentFactory
import net.ntworld.mergeRequestIntegrationIde.toolWindow.CommentsToolWindowTab
import net.ntworld.mergeRequestIntegrationIde.toolWindow.FilesToolWindowTab

interface ComponentFactory {
    val toolWindowTabs: ToolWindowTabFactory

    val commentComponents: CommentComponentFactory

    fun makePaginationToolbar(displayRefreshButton: Boolean = false): PaginationToolbar

    interface ToolWindowTabFactory {
        fun makeFilesToolWindowTab(providerData: ProviderData, isCodeReviewChanges: Boolean): FilesToolWindowTab

        fun makeCommentsToolWindowTab(
            providerData: ProviderData,
            mergeRequestInfo: MergeRequestInfo,
            comments: List<Comment>
        ): CommentsToolWindowTab
    }
}