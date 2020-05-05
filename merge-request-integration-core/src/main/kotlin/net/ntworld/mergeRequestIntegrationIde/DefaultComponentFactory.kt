package net.ntworld.mergeRequestIntegrationIde

import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequest.MergeRequestInfo
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequestIntegrationIde.component.PaginationToolbar
import net.ntworld.mergeRequestIntegrationIde.component.PaginationToolbarImpl
import net.ntworld.mergeRequestIntegrationIde.infrastructure.ProjectServiceProvider
import net.ntworld.mergeRequestIntegrationIde.toolWindow.CommentsToolWindowTab
import net.ntworld.mergeRequestIntegrationIde.toolWindow.FilesToolWindowTab
import net.ntworld.mergeRequestIntegrationIde.toolWindow.internal.CommentsToolWindowTabImpl
import net.ntworld.mergeRequestIntegrationIde.toolWindow.internal.FilesToolWindowTabImpl

class DefaultComponentFactory(
    private val projectServiceProvider: ProjectServiceProvider
) : ComponentFactory {
    override val toolWindowTabs: ComponentFactory.ToolWindowTabFactory = MyToolWindowTabFactory(projectServiceProvider)

    override fun makePaginationToolbar(displayRefreshButton: Boolean): PaginationToolbar {
        return PaginationToolbarImpl(displayRefreshButton)
    }

    private class MyToolWindowTabFactory(
        private val projectServiceProvider: ProjectServiceProvider
    ) : ComponentFactory.ToolWindowTabFactory {
        override fun makeFilesToolWindowTab(
            providerData: ProviderData,
            isCodeReviewChanges: Boolean
        ): FilesToolWindowTab {
            return FilesToolWindowTabImpl(projectServiceProvider, providerData, isCodeReviewChanges)
        }

        override fun makeCommentsToolWindowTab(
            providerData: ProviderData,
            mergeRequestInfo: MergeRequestInfo,
            comments: List<Comment>
        ): CommentsToolWindowTab {
            return CommentsToolWindowTabImpl(projectServiceProvider, providerData, mergeRequestInfo, comments)
        }
    }
}