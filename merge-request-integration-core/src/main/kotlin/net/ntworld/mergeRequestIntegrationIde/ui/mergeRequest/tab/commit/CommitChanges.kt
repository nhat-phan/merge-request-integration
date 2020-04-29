package net.ntworld.mergeRequestIntegrationIde.ui.mergeRequest.tab.commit

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.vcs.changes.Change
import com.intellij.openapi.vcs.changes.ui.ChangesBrowserChangeNode
import com.intellij.openapi.vcs.changes.ui.ChangesTreeImpl
import com.intellij.openapi.vcs.changes.ui.TreeModelBuilder
import com.intellij.ui.ScrollPaneFactory
import net.miginfocom.swing.MigLayout
import net.ntworld.mergeRequest.Commit
import net.ntworld.mergeRequest.MergeRequestInfo
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequestIntegrationIde.infrastructure.ProjectServiceProvider
import net.ntworld.mergeRequestIntegrationIde.ui.util.CustomSimpleToolWindowPanel
import net.ntworld.mergeRequestIntegrationIde.ui.util.ToolbarUtil
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.event.TreeSelectionListener
import javax.swing.tree.DefaultTreeModel
import kotlin.concurrent.thread
import com.intellij.openapi.project.Project as IdeaProject

class CommitChanges(private val projectServiceProvider: ProjectServiceProvider) : CommitChangesUI {
    private val myComponent = CustomSimpleToolWindowPanel(vertical = true, borderless = true)
    private val myTree = MyTree(projectServiceProvider.project)
    private var myProviderData: ProviderData? = null
    private var myMergeRequestInfo: MergeRequestInfo? = null
    private val myTreeSelectionListener = TreeSelectionListener {
        if (null !== it && myTree.isVisible) {
            val lastPath = it.path.lastPathComponent
            val providerData = myProviderData
            val mergeRequestInfo = myMergeRequestInfo
            if (null !== providerData && null !== mergeRequestInfo && lastPath is ChangesBrowserChangeNode) {
                val selectedContext = projectServiceProvider.reviewContextManager.findSelectedContext()
                val reviewContext = projectServiceProvider.reviewContextManager.findContext(
                    providerData.id, mergeRequestInfo.id
                )
                if (null === reviewContext) {
                    return@TreeSelectionListener
                }
                if (null !== selectedContext) {
                    if (selectedContext.providerData.id != reviewContext.providerData.id ||
                        selectedContext.mergeRequestInfo.id != reviewContext.mergeRequestInfo.id) {
                        return@TreeSelectionListener
                    }
                }
                reviewContext.openChange(lastPath.userObject, focus = false, displayMergeRequestId = !projectServiceProvider.isDoingCodeReview())
            }
        }
    }

    init {
        myComponent.setContent(ScrollPaneFactory.createScrollPane(myTree, true))
        myComponent.toolbar = createToolbar()
        myTree.addTreeSelectionListener(myTreeSelectionListener)
    }

    override fun clear() {
        ApplicationManager.getApplication().invokeLater {
            myTree.setChangesToDisplay(listOf())
        }
    }

    override fun disable() {
        myComponent.isVisible = false
    }

    override fun enable() {
        myComponent.isVisible = true
    }

    override fun setCommits(providerData: ProviderData, mergeRequestInfo: MergeRequestInfo, commits: List<Commit>) {
        myProviderData = providerData
        myMergeRequestInfo = mergeRequestInfo
        thread {
            myTree.isVisible = false
            val changes = projectServiceProvider.repositoryFile.findChanges(providerData, commits.map { it.id })
            projectServiceProvider.reviewContextManager.updateChanges(providerData.id, mergeRequestInfo.id, changes)
            projectServiceProvider.reviewContextManager.updateReviewingChanges(providerData.id, mergeRequestInfo.id, changes)
            ApplicationManager.getApplication().invokeLater {
                myTree.setChangesToDisplay(changes)
                myTree.isVisible = true
            }
        }
    }

    override fun updateSelectedCommits(
        providerData: ProviderData,
        mergeRequestInfo: MergeRequestInfo,
        selectedCommits: List<Commit>
    ) {
        myProviderData = providerData
        myMergeRequestInfo = mergeRequestInfo
        thread {
            myTree.isVisible = false
            val partialChanges = projectServiceProvider.repositoryFile.findChanges(providerData, selectedCommits.map { it.id })
            projectServiceProvider.reviewContextManager.updateReviewingChanges(
                providerData.id, mergeRequestInfo.id, partialChanges
            )
            ApplicationManager.getApplication().invokeLater {
                myTree.setChangesToDisplay(partialChanges)
                myTree.isVisible = true
            }
        }
    }

    override fun createComponent(): JComponent = myComponent

    private fun createToolbar(): JComponent {
        val panel = JPanel(MigLayout("ins 0, fill", "[left]push[right]", "center"))

        val leftActionGroup = DefaultActionGroup()
        val leftToolbar = ActionManager.getInstance().createActionToolbar(
            "${CommitChanges::class.java.canonicalName}/toolbar-left",
            leftActionGroup,
            true
        )

        panel.add(leftToolbar.component)
        panel.add(
            ToolbarUtil.createExpandAndCollapseToolbar(
                "${CommitChanges::class.java.canonicalName}/toolbar-right",
                myTree
            )
        )
        return panel
    }

    private class MyTree(ideaProject: IdeaProject) : ChangesTreeImpl<Change>(
        ideaProject, false, false, Change::class.java
    ) {
        override fun buildTreeModel(changes: MutableList<out Change>): DefaultTreeModel {
            return TreeModelBuilder.buildFromChanges(myProject, grouping, changes, null)
        }
    }
}