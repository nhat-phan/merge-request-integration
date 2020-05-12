package net.ntworld.mergeRequestIntegrationIde.toolWindow

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.vcs.changes.Change
import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.content.Content
import com.intellij.ui.content.ContentFactory
import com.intellij.util.ContentUtilEx
import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequest.MergeRequestInfo
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequestIntegrationIde.SINGLE_MR_CHANGES_WHEN_DOING_CODE_REVIEW_NAME
import net.ntworld.mergeRequestIntegrationIde.SINGLE_MR_REWORK_CHANGES_PREFIX
import net.ntworld.mergeRequestIntegrationIde.SINGLE_MR_REWORK_COMMENTS_PREFIX
import net.ntworld.mergeRequestIntegrationIde.debug
import net.ntworld.mergeRequestIntegrationIde.infrastructure.ProjectServiceProvider
import net.ntworld.mergeRequestIntegrationIde.infrastructure.notifier.SingleMRToolWindowNotifier
import net.ntworld.mergeRequestIntegrationIde.rework.ReworkWatcher

class SingleMRToolWindowManager(
    private val projectServiceProvider: ProjectServiceProvider,
    private val toolWindow: ToolWindow
) : SingleMRToolWindowNotifier {
    private var myReviewChangesTab: FilesToolWindowTab? = null
    private val myReworkChangesTabs = mutableSetOf<FilesToolWindowTab>()
    private val myReworkCommentsTabs = mutableSetOf<CommentsToolWindowTab>()
    private val mySupportedProviders = mutableMapOf<String, String>()

    init {
        projectServiceProvider.messageBus.connect().subscribe(SingleMRToolWindowNotifier.TOPIC, this)
        val activeReworkWatchers = projectServiceProvider.reworkManager.getActiveReworkWatchers()
        activeReworkWatchers.forEach {
            registerReworkWatcher(it)
            if (it.isChangesBuilt()) {
                showReworkChanges(it, it.changes)
            }
            if (it.isFetchedComments()) {
                showReworkComments(it, it.comments, it.displayResolvedComments)
            }
        }
    }

    override fun registerReworkWatcher(reworkWatcher: ReworkWatcher) {
        val key = reworkWatcher.key()
        debug("$key: add watcher to SupportedProviders")
        mySupportedProviders[reworkWatcher.providerData.id] = reworkWatcher.branchName
    }

    override fun removeReworkWatcher(reworkWatcher: ReworkWatcher) {
        val key = reworkWatcher.key()
        val currentBranch = mySupportedProviders[reworkWatcher.providerData.id]
        if (currentBranch == reworkWatcher.branchName) {
            debug("$key: remove ReworkWatcher out SupportedWatchers")
            mySupportedProviders.remove(reworkWatcher.providerData.id)
            ApplicationManager.getApplication().invokeLater {
                debug("$key: remove tab out of tool window")

                val tab = myReworkChangesTabs.firstOrNull { it.providerData.id == reworkWatcher.providerData.id }
                if (null !== tab) {
                    removeTabOutOfToolWindow(tab, myReworkChangesTabs, SINGLE_MR_REWORK_CHANGES_PREFIX)
                }

                val commentsTab = myReworkCommentsTabs.firstOrNull { it.providerData.id == reworkWatcher.providerData.id }
                if (null !== commentsTab) {
                    removeTabOutOfToolWindow(commentsTab, myReworkCommentsTabs, SINGLE_MR_REWORK_COMMENTS_PREFIX)
                }

                if (myReworkChangesTabs.isEmpty() && myReworkCommentsTabs.isEmpty()) {
                    projectServiceProvider.hideSingleMRToolWindow {
                        debug("$key: hide tool window because there is nothing left")
                    }
                }
            }
        } else {
            debug("$key: current provider have another branch, do nothing")
        }
    }

    override fun hideChangesAfterDoingCodeReview() {
        val reviewTab = myReviewChangesTab
        if (null !== reviewTab) {
            reviewTab.hide()
        }
    }

    override fun clearReworkTabs() {
        removeAllReworkToolWindowTabs()
    }

    override fun showChangesWhenDoingCodeReview(providerData: ProviderData, changes: List<Change>) {
        removeAllReworkToolWindowTabs()

        val currentTab = myReviewChangesTab
        if (null !== currentTab) {
            currentTab.setChanges(providerData, changes)
            return
        }

        val newTab = makeFilesToolWindowTab(providerData, true)
        newTab.setChanges(providerData, changes)
        toolWindow.contentManager.addContent(
            ContentFactory.SERVICE.getInstance().createContent(
                newTab.component,
                SINGLE_MR_CHANGES_WHEN_DOING_CODE_REVIEW_NAME,
                true
            )
        )
        myReviewChangesTab = newTab
    }

    override fun showReworkChanges(
        reworkWatcher: ReworkWatcher, changes: List<Change>
    ) = fillingDataForReworkTab(reworkWatcher) {
        val currentTab = myReworkChangesTabs.firstOrNull { it.providerData.id == reworkWatcher.providerData.id }
        if (null !== currentTab) {
            debug("${reworkWatcher.key()}: update current Files tab's data")
            currentTab.setChanges(reworkWatcher.providerData, changes)
            return@fillingDataForReworkTab
        }

        val newTab = makeFilesToolWindowTab(reworkWatcher.providerData, false)
        newTab.setChanges(reworkWatcher.providerData, changes)
        debug("${reworkWatcher.key()}: add new Files tab of  to tool window")
        addTabToToolWindow(newTab, myReworkChangesTabs, SINGLE_MR_REWORK_CHANGES_PREFIX)
    }

    override fun showReworkComments(
        reworkWatcher: ReworkWatcher,
        comments: List<Comment>,
        displayResolvedComments: Boolean
    ) = fillingDataForReworkTab(reworkWatcher) {
        val currentTab = myReworkCommentsTabs.firstOrNull { it.providerData.id == reworkWatcher.providerData.id }
        if (null !== currentTab) {
            debug("${reworkWatcher.key()}: update current Comments tab's data")
            currentTab.setMergeRequestInfo(reworkWatcher.mergeRequestInfo)
            currentTab.setDisplayResolvedComments(displayResolvedComments)
            currentTab.setComments(comments)
            return@fillingDataForReworkTab
        }

        val newTab = makeCommentsToolWindowTab(reworkWatcher.providerData, reworkWatcher.mergeRequestInfo, comments)
        debug("${reworkWatcher.key()}: add new Comments tab to tool window")
        addTabToToolWindow(newTab, myReworkCommentsTabs, SINGLE_MR_REWORK_COMMENTS_PREFIX)
    }

    private fun removeToolWindowTabsForCodeReview() {
        val content = toolWindow.contentManager.findContent(SINGLE_MR_CHANGES_WHEN_DOING_CODE_REVIEW_NAME)
        if (null !== content) {
            toolWindow.contentManager.removeContent(content, true)
            myReviewChangesTab = null
        }
    }

    private fun fillingDataForReworkTab(reworkWatcher: ReworkWatcher, invoker: (() -> Unit)) {
        val key = reworkWatcher.key()
        if (mySupportedProviders.contains(reworkWatcher.providerData.id)) {
            removeToolWindowTabsForCodeReview()
            debug("$key: ReworkWatcher is supported")
            invoker()
        } else {
            debug("$key: ReworkWatcher is NOT supported")
        }
    }

    private fun <T> addTabToToolWindow(
        tab: T,
        bucket: MutableSet<T>,
        prefix: String
    ): Boolean where T : ReworkToolWindowTab {
        // There are 3 scenarios when removing a tab:
        //  A) 0 -> 1: we should add content
        //  B) 1 -> 2: we should remove content, add old tab to tabbed content
        //  C) 2 -> n: just add tabbed content
        if (bucket.isEmpty()) {
            toolWindow.contentManager.addContent(
                ContentFactory.SERVICE.getInstance().createContent(tab.component, prefix, true)
            )
            return bucket.add(tab)
        }

        if (bucket.size == 1) {
            val oldTab = bucket.first()
            val oldContent = toolWindow.contentManager.findContent(prefix)
            toolWindow.contentManager.removeContent(oldContent, false)

            ContentUtilEx.addTabbedContent(
                toolWindow.contentManager,
                oldContent.component,
                prefix,
                oldTab.providerData.name,
                true,
                null
            )
        }

        ContentUtilEx.addTabbedContent(
            toolWindow.contentManager,
            tab.component,
            prefix,
            tab.providerData.name,
            true,
            null
        )
        return bucket.add(tab)
    }

    private fun <T> removeTabOutOfToolWindow(
        tab: T,
        bucket: MutableSet<T>,
        prefix: String
    ) where T : ReworkToolWindowTab {
        // There are 3 scenarios when removing a tab:
        //  A) 1 -> 0: we should remove content
        //  C) 2 -> 1: we should remove tabbed content, create new content with remaining tab
        //  B) n -> 2: just remove tab of tabbed content
        if (!bucket.contains(tab)) {
            return
        }

        if (bucket.size == 1) {
            val content = toolWindow.contentManager.findContent(prefix)
            toolWindow.contentManager.removeContent(content, true)
            return bucket.clear()
        }

        val tabbedContent = ContentUtilEx.findTabbedContent(toolWindow.contentManager, prefix)
        if (null === tabbedContent) {
            return
        }

        if (bucket.size == 2) {
            toolWindow.contentManager.removeContent(tabbedContent, false)
            bucket.remove(tab)

            val remainingTab = bucket.first()
            toolWindow.contentManager.addContent(
                ContentFactory.SERVICE.getInstance().createContent(remainingTab.component, prefix, true)
            )
            return
        }

        val tabInTabbedContent = tabbedContent.tabs.firstOrNull { it.second === tab.component }
        if (null !== tabInTabbedContent) {
            bucket.remove(tab)
            tabbedContent.tabs.remove(tabInTabbedContent)
        }
    }

    private fun removeAllReworkToolWindowTabs() {
        // There are 2 scenarios when removing all tab:
        //  A) bucket > 1: we should remove tabbed content
        //  B) bucket = 1: we should remove content
        val changes = if (myReworkChangesTabs.size > 1) {
            ContentUtilEx.findTabbedContent(toolWindow.contentManager, SINGLE_MR_REWORK_CHANGES_PREFIX)
        } else {
            toolWindow.contentManager.findContent(SINGLE_MR_REWORK_CHANGES_PREFIX)
        }
        if (null !== changes) {
            toolWindow.contentManager.removeContent(changes, true)
        }
        myReworkChangesTabs.clear()

        val comments = if (myReworkCommentsTabs.size > 1) {
            ContentUtilEx.findTabbedContent(toolWindow.contentManager, SINGLE_MR_REWORK_COMMENTS_PREFIX)
        } else {
            toolWindow.contentManager.findContent(SINGLE_MR_REWORK_COMMENTS_PREFIX)
        }
        if (null !== comments) {
            toolWindow.contentManager.removeContent(comments, true)
        }
        myReworkCommentsTabs.clear()
    }

    private fun makeFilesToolWindowTab(providerData: ProviderData, isCodeReviewChange: Boolean): FilesToolWindowTab {
        return projectServiceProvider.componentFactory.toolWindowTabs.makeFilesToolWindowTab(
            providerData, isCodeReviewChange
        )
    }

    private fun makeCommentsToolWindowTab(
        providerData: ProviderData, mergeRequestInfo: MergeRequestInfo, comments: List<Comment>
    ): CommentsToolWindowTab {
        return projectServiceProvider.componentFactory.toolWindowTabs.makeCommentsToolWindowTab(
            providerData, mergeRequestInfo, comments
        )
    }

    private data class ComponentContainer(
        val content: Content,
        val isTabbed: Boolean
    )
}