package net.ntworld.mergeRequestIntegrationIde.toolWindow.internal

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.vcs.changes.Change
import com.intellij.openapi.vcs.changes.ui.ChangesTreeImpl
import com.intellij.openapi.vcs.changes.ui.TreeModelBuilder
import com.intellij.ui.JBColor
import com.intellij.ui.ScrollPaneFactory
import net.miginfocom.swing.MigLayout
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequestIntegrationIde.infrastructure.ProjectServiceProvider
import net.ntworld.mergeRequestIntegrationIde.mergeRequest.comments.tree.CommentTreeView
import net.ntworld.mergeRequestIntegrationIde.toolWindow.FilesToolWindowTab
import net.ntworld.mergeRequestIntegrationIde.ui.mergeRequest.tab.commit.CommitChanges
import net.ntworld.mergeRequestIntegrationIde.ui.util.CustomSimpleToolWindowPanel
import net.ntworld.mergeRequestIntegrationIde.ui.util.ToolbarUtil
import java.awt.GridBagLayout
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.SwingConstants
import javax.swing.event.TreeSelectionListener
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreePath

class FilesToolWindowTabImpl(
    private val projectServiceProvider: ProjectServiceProvider,
    override val providerData: ProviderData,
    override val isCodeReviewChanges: Boolean
) : FilesToolWindowTab {
    private var myProviderData: ProviderData? = null
    private val myComponentEmpty = JPanel()
    private val myLabelEmpty = JLabel("", SwingConstants.CENTER)
    private val myComponent = CustomSimpleToolWindowPanel(vertical = true, borderless = true)
    private val myTree = MyTree(projectServiceProvider.project)
    private val myTreeWrapper = ScrollPaneFactory.createScrollPane(myTree, true)
    private val myToolbar by lazy {
        val panel = JPanel(MigLayout("ins 0, fill", "[left]0[left, fill]push[right]", "center"))

        val leftActionGroup = DefaultActionGroup()
        val leftToolbar = ActionManager.getInstance().createActionToolbar(
            "${CommitChanges::class.java.canonicalName}/toolbar-left",
            leftActionGroup,
            true
        )

        panel.add(JPanel())
        panel.add(leftToolbar.component)
        panel.add(
            ToolbarUtil.createExpandAndCollapseToolbar(
                "${this::class.java.canonicalName}/toolbar-right",
                myTree
            )
        )
        panel
    }
    private val myTreeMouseListener = object : MouseAdapter() {
        override fun mousePressed(e: MouseEvent?) {
            if (null === e) {
                return
            }

            if (e.clickCount == 2) {
                handleTreeItemSelected(myTree.selectionPath)
            }
        }
    }
    private val myKeyListener = object: KeyAdapter() {
        override fun keyPressed(e: KeyEvent?) {
            if (null === e) {
                return
            }
            if (e.keyCode == KeyEvent.VK_ENTER) {
                handleTreeItemSelected(myTree.selectionPath)
            }
        }
    }

    init {
        myLabelEmpty.text = "<html>Files' changes will be displayed when you do Code Review<br/>or<br/>open a branch which has an opened Merge Request</html>"
        myComponentEmpty.background = JBColor.background()
        myComponentEmpty.layout = GridBagLayout()
        myComponentEmpty.add(myLabelEmpty)
        myComponent.toolbar = myToolbar
        myTree.addMouseListener(myTreeMouseListener)
        myTree.addKeyListener(myKeyListener)

        val reviewContext = projectServiceProvider.reviewContextManager.findDoingCodeReviewContext()
        if (null !== reviewContext) {
            setChanges(reviewContext.providerData, reviewContext.reviewingChanges)
        } else {
            hide()
        }
    }

    override val component: JComponent = myComponent

    private fun handleTreeItemSelected(path: TreePath?) {
        if (null === path) {
            return
        }

        val node = path.lastPathComponent as? DefaultMutableTreeNode ?: return
        val change = node.userObject as? Change ?: return
        val reviewContext = projectServiceProvider.reviewContextManager.findDoingCodeReviewContext()
        if (null !== reviewContext) {
            reviewContext.openChange(change, focus = true, displayMergeRequestId = false)
            return
        }

        val providerData = myProviderData
        if (null !== providerData) {
            val reworkWatcher = projectServiceProvider.reworkManager.findReworkWatcherByChange(
                providerData,
                change
            )
            if (null !== reworkWatcher) {
                reworkWatcher.openChange(change)
            }
        }
    }

    override fun setChanges(providerData: ProviderData, changes: List<Change>) {
        myProviderData = providerData
        ApplicationManager.getApplication().invokeLater {
            myTree.setChangesToDisplay(changes)
        }
        myToolbar.isVisible = true
        myComponent.setContent(myTreeWrapper)
    }

    override fun hide() {
        myTree.setChangesToDisplay(listOf())
        myToolbar.isVisible = false
        myComponent.setContent(myComponentEmpty)
    }

    private class MyTree(ideaProject: Project) : ChangesTreeImpl<Change>(
        ideaProject, false, false, Change::class.java
    ) {
        override fun buildTreeModel(changes: MutableList<out Change>): DefaultTreeModel {
            return TreeModelBuilder.buildFromChanges(myProject, grouping, changes, null)
        }
    }
}