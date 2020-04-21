package net.ntworld.mergeRequestIntegrationIde.ui.mergeRequest

import com.intellij.ide.util.treeView.NodeRenderer
import com.intellij.openapi.application.ApplicationManager
import com.intellij.ui.ScrollPaneFactory
import com.intellij.ui.treeStructure.Tree
import com.intellij.openapi.project.Project as IdeaProject
import net.ntworld.mergeRequest.MergeRequestInfo
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequestIntegrationIde.service.ApplicationService
import java.awt.Component
import javax.swing.JComponent
import javax.swing.JTree
import javax.swing.event.TreeSelectionEvent
import javax.swing.event.TreeSelectionListener
import javax.swing.tree.*

class MergeRequestCollectionTree(
    private val applicationService: ApplicationService,
    private val ideaProject: IdeaProject,
    private val providerData: ProviderData
) : AbstractMergeRequestCollection(applicationService, ideaProject, providerData), TreeCellRenderer {

    private val myTree = Tree()
    private val myRoot = DefaultMutableTreeNode()
    private val myRenderer = NodeRenderer()
    private val myModel = DefaultTreeModel(myRoot)
    private val myTreeSelectionListener = object : TreeSelectionListener {
        override fun valueChanged(e: TreeSelectionEvent?) {
            if (null === e) {
                return
            }

            val path = e.path
            when (val node = (path.lastPathComponent as DefaultMutableTreeNode).userObject) {
                is MergeRequestCollectionTreeNode -> {
                    eventDispatcher.multicaster.mergeRequestSelected(
                        providerData = providerData,
                        mergeRequestInfo = node.mergeRequestInfo
                    )
                }
            }
        }
    }

    init {
        val treeSelectionModel = DefaultTreeSelectionModel()
        treeSelectionModel.selectionMode = TreeSelectionModel.SINGLE_TREE_SELECTION

        myTree.model = myModel
        myTree.cellRenderer = this
        myTree.isRootVisible = false
        myTree.selectionModel = treeSelectionModel
        myTree.addTreeSelectionListener(myTreeSelectionListener)
    }

    override fun makeContent(): JComponent {
        return ScrollPaneFactory.createScrollPane(myTree)
    }

    override fun fetchDataStarted() {
        myRoot.removeAllChildren()
        myTree.isVisible = false
    }

    override fun fetchDataStopped() {
        myTree.isVisible = true
    }

    override fun dataReceived(collection: List<MergeRequestInfo>) {
        ApplicationManager.getApplication().invokeLater {
            myRoot.removeAllChildren()
            collection.forEach {
                val item = MergeRequestCollectionTreeNode(
                    providerData,
                    ideaProject,
                    it
                )
                myRoot.add(DefaultMutableTreeNode(item))
                item.update()
            }
            myModel.nodeStructureChanged(myRoot)
            myTree.isVisible = true
        }
    }

    override fun getTreeCellRendererComponent(
        tree: JTree?,
        value: Any?,
        selected: Boolean,
        expanded: Boolean,
        leaf: Boolean,
        row: Int,
        hasFocus: Boolean
    ): Component {
        return myRenderer.getTreeCellRendererComponent(
            tree,
            value,
            selected,
            expanded,
            leaf,
            row,
            hasFocus
        )
    }

}