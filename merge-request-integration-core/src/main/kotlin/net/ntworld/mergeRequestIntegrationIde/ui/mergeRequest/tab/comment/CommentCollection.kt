package net.ntworld.mergeRequestIntegrationIde.ui.mergeRequest.tab.comment

import com.intellij.icons.AllIcons
import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.util.treeView.NodeRenderer
import com.intellij.ide.util.treeView.PresentableNodeDescriptor
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.util.Iconable
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.ui.ScrollPaneFactory
import com.intellij.ui.SimpleTextAttributes
import com.intellij.openapi.project.Project as IdeaProject
import com.intellij.ui.treeStructure.Tree
import com.intellij.util.EventDispatcher
import com.intellij.util.ui.tree.TreeUtil
import git4idea.repo.GitRepository
import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequest.CommentPosition
import net.ntworld.mergeRequest.MergeRequest
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequestIntegration.util.DateTimeUtil
import net.ntworld.mergeRequestIntegrationIde.internal.CommentStoreItem
import net.ntworld.mergeRequestIntegrationIde.service.CodeReviewUtil
import net.ntworld.mergeRequestIntegrationIde.service.CommentStore
import net.ntworld.mergeRequestIntegrationIde.service.ProjectEventListener
import net.ntworld.mergeRequestIntegrationIde.service.ProjectService
import net.ntworld.mergeRequestIntegrationIde.ui.util.CustomSimpleToolWindowPanel
import net.ntworld.mergeRequestIntegrationIde.ui.util.Icons
import net.ntworld.mergeRequestIntegrationIde.ui.util.RepositoryUtil
import java.awt.Component
import javax.swing.Icon
import javax.swing.JComponent
import javax.swing.JTree
import javax.swing.event.TreeSelectionEvent
import javax.swing.event.TreeSelectionListener
import javax.swing.tree.*

class CommentCollection(
    private val ideaProject: IdeaProject
) : CommentCollectionUI, TreeCellRenderer {
    override val dispatcher = EventDispatcher.create(CommentCollectionUI.Listener::class.java)

    private val projectService = ProjectService.getInstance(ideaProject)
    private var myProviderData: ProviderData? = null
    private var myMergeRequest: MergeRequest? = null
    private var myComments: List<Comment>? = null
    private var mySelectedTreeNode: DefaultMutableTreeNode? = null
    private var myShowResolved = false
    private val myTree = Tree()
    private val myComponent = CustomSimpleToolWindowPanel(vertical = true, borderless = true)
    private val myRoot = DefaultMutableTreeNode()
    private val myRenderer = NodeRenderer()
    private val myModel = DefaultTreeModel(myRoot)
    private val myProjectListener = object : ProjectEventListener {
        override fun newCommentRequested(
            providerData: ProviderData,
            mergeRequest: MergeRequest,
            position: CommentPosition,
            item: CommentStore.Item
        ) {
            val comments = myComments
            if (null !== comments) {
                filterAndDisplayComments(providerData, comments, myShowResolved) {
                    findEditorNodeByItemAndGrabFocus(item, myRoot)
                }
            }
        }
    }
    private val myTreeSelectionListener = object : TreeSelectionListener {
        override fun valueChanged(e: TreeSelectionEvent?) {
            if (null === e) {
                return
            }

            val path = e.path
            val treeNode = path.lastPathComponent as DefaultMutableTreeNode
            mySelectedTreeNode = treeNode
            when (val node = treeNode.userObject) {
                is CommentNode -> {
                    val providerData = myProviderData
                    val mergeRequest = myMergeRequest
                    if (null !== providerData && null !== mergeRequest) {
                        dispatcher.multicaster.commentSelected(providerData, mergeRequest, node.comment)
                    }
                }
                is EditorNode -> {
                    dispatcher.multicaster.commentUnselected()
                    val providerData = myProviderData
                    val mergeRequest = myMergeRequest
                    if (null !== providerData && null !== mergeRequest) {
                        dispatcher.multicaster.editorSelected(
                            providerData,
                            mergeRequest,
                            node.comment,
                            node.item
                        )
                    }
                }
                is GroupNode -> {
                    dispatcher.multicaster.commentUnselected()
                }
            }
        }
    }
    private val myFilterToolbar: CommentCollectionFilterUI = CommentCollectionFilter()
    private val myFilterToolbarEventListener = object : CommentCollectionFilterUI.Listener {
        override fun onFiltersChanged(showResolved: Boolean) {
            val providerData = myProviderData
            val comments = myComments
            if (null !== providerData && null != comments) {
                filterAndDisplayComments(providerData, comments, showResolved)
            }
        }

        override fun onRefreshButtonClicked() {
            val mr = myMergeRequest
            if (null !== mr) {
                dispatcher.multicaster.refreshRequested(mr)
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

        myComponent.setContent(ScrollPaneFactory.createScrollPane(myTree))
        myComponent.toolbar = myFilterToolbar.createComponent()

        myFilterToolbar.dispatcher.addListener(myFilterToolbarEventListener)
        projectService.dispatcher.addListener(myProjectListener)
    }

    override fun setComments(providerData: ProviderData, mergeRequest: MergeRequest, comments: List<Comment>) {
        myProviderData = providerData
        myMergeRequest = mergeRequest
        myComments = comments
        filterAndDisplayComments(providerData, comments, false)
    }

    override fun createReplyComment() {
        val providerData = myProviderData
        val mergeRequest = myMergeRequest
        if (null === providerData || null === mergeRequest) {
            return
        }

        val selectedTreeNode = mySelectedTreeNode
        if (null === selectedTreeNode) {
            return
        }
        val parentTreeNode = selectedTreeNode.parent as DefaultMutableTreeNode

        val parentNode = parentTreeNode.userObject
        val selectedNode = selectedTreeNode.userObject
        if (parentNode !is GroupNode || selectedNode !is CommentNode) {
            return
        }

        val lastChild = parentTreeNode.lastChild
        if (lastChild !is DefaultMutableTreeNode || lastChild.userObject is EditorNode) {
            return
        }

        val storeItem = CommentStoreItem.createReplyItem(
            providerData, mergeRequest, parentNode.data.nodeData, selectedNode.comment
        )
        projectService.commentStore.add(storeItem)

        appendEditorNode(storeItem, selectedNode.comment, parentTreeNode, true)
        myModel.nodeStructureChanged(parentTreeNode)
    }

    private fun appendEditorNode(
        storeItem: CommentStore.Item,
        comment: Comment?,
        parentTreeNode: DefaultMutableTreeNode,
        select: Boolean
    ) {
        val presentation = EditorNode(ideaProject, storeItem, comment)
        presentation.update()
        val node = DefaultMutableTreeNode(presentation)
        parentTreeNode.add(node)
        if (select) {
            myTree.selectionPath = TreeUtil.getPath(myRoot, node)
        }
    }

    fun findEditorNodeByItemAndGrabFocus(item: CommentStore.Item, parent: DefaultMutableTreeNode) {
        for (child in parent.children()) {
            if (child !is DefaultMutableTreeNode) {
                continue
            }
            val userObject = child.userObject
            if (userObject !is EditorNode) {
                findEditorNodeByItemAndGrabFocus(item, child)
                continue
            }
            if (userObject.item.id == item.id) {
                myTree.selectionPath = TreeUtil.getPath(myRoot, child)
                return
            }
        }
    }

    private fun filterAndDisplayComments(
        providerData: ProviderData,
        comments: List<Comment>,
        showResolved: Boolean,
        callback: (() -> Unit)? = null
    ) {
        myShowResolved = showResolved
        if (showResolved) {
            ApplicationManager.getApplication().invokeLater {
                displayComments(providerData, comments)
                if (null !== callback) {
                    callback.invoke()
                }
            }
        } else {
            ApplicationManager.getApplication().invokeLater {
                displayComments(providerData, comments.filter { !it.resolved })
                if (null !== callback) {
                    callback.invoke()
                }
            }
        }
    }

    private fun displayComments(
        providerData: ProviderData,
        comments: List<Comment>
    ) {
        val repository = RepositoryUtil.findRepository(ideaProject, providerData)
        val grouped = groupCommentsByPathAndLine(comments)
        myRoot.removeAllChildren()
        for (item in grouped) {
            val groupNodePresentation = GroupNode(ideaProject, repository, item)
            val groupNode = DefaultMutableTreeNode(groupNodePresentation)
            for (comment in item.comments) {
                val commentNodePresentation = CommentNode(ideaProject, comment)
                val commentNode = DefaultMutableTreeNode(commentNodePresentation)
                groupNode.add(commentNode)
                commentNodePresentation.update()
            }
            findAndAppendReplyNode(item.nodeData.getHash(), groupNode, item.comments)
            findAndAppendNewNode(item.nodeData.getHash(), groupNode)
            myRoot.add(groupNode)
            groupNodePresentation.update()
        }
        dispatcher.multicaster.commentsDisplayed(comments.size)
        myTree.selectionPath = TreeUtil.getPath(myRoot, myRoot)
        myModel.nodeStructureChanged(myRoot)
        TreeUtil.expandAll(myTree)
    }

    private fun findAndAppendReplyNode(
        groupPath: String,
        groupTreeNode: DefaultMutableTreeNode,
        comments: List<Comment>
    ) {
        val replyItem = projectService.commentStore.findReplyItem(groupPath)
        if (null === replyItem) {
            return
        }
        val comment = comments.firstOrNull { it.id == replyItem.commentId }
        if (null === comment) {
            return
        }
        appendEditorNode(replyItem, comment, groupTreeNode, false)
    }

    private fun findAndAppendNewNode(groupPath: String, groupTreeNode: DefaultMutableTreeNode) {
        val newItem = projectService.commentStore.findNewItem(groupPath)
        if (null === newItem) {
            return
        }
        appendEditorNode(newItem, null, groupTreeNode, false)
    }

    private fun groupCommentsByPathAndLine(comments: List<Comment>): Collection<GroupedComments> {
        val result = mutableMapOf<String, GroupedComments>()
        val newItems = projectService.commentStore.getNewItems()
        for (item in newItems) {
            if (null === item.position) {
                continue
            }

            val nodeData = projectService.codeReviewUtil.convertPositionToCommentNodeData(item.position!!)
            result[item.groupNodePath.first()] = GroupedComments(
                nodeData = nodeData,
                comments = mutableListOf()
            )
        }

        for (comment in comments) {
            val nodeData = projectService.codeReviewUtil.findCommentNodeData(comment)
            val item = result[nodeData.getHash()]
            if (null === item) {
                result[nodeData.getHash()] = GroupedComments(
                    nodeData = nodeData,
                    comments = mutableListOf(comment)
                )
            } else {
                (item.comments as MutableList<Comment>).add(comment)
            }
        }
        return result.toSortedMap().values
    }

    override fun createComponent(): JComponent = myComponent

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

    private data class GroupedComments(
        val comments: List<Comment>,
        val nodeData: CodeReviewUtil.CommentNodeData
    )

    private class CommentNode(
        private val ideaProject: IdeaProject,
        val comment: Comment
    ) : PresentableNodeDescriptor<Comment>(ideaProject, null) {
        override fun update(presentation: PresentationData) {
            presentation.setIcon(
                if (comment.resolved) {
                    Icons.Approved
                } else {
                    Icons.RequiredApproval
                }
            )
            presentation.addText(
                "${DateTimeUtil.toPretty(DateTimeUtil.toDate(comment.createdAt))} · ",
                SimpleTextAttributes.GRAYED_ATTRIBUTES
            )
            presentation.addText(comment.author.name, SimpleTextAttributes.REGULAR_ATTRIBUTES)
            presentation.addText(" @${comment.author.username}", SimpleTextAttributes.GRAYED_ATTRIBUTES)
        }

        override fun getElement(): Comment = comment
    }

    private class EditorNode(
        private val ideaProject: IdeaProject,
        val item: CommentStore.Item,
        val comment: Comment?
    ) : PresentableNodeDescriptor<CommentStore.Item>(ideaProject, null) {
        override fun update(presentation: PresentationData) {
            presentation.setIcon(Icons.ReplyComment)
            when (item.type) {
                CommentStore.ItemType.EDIT -> presentation.addText("Editing", SimpleTextAttributes.REGULAR_ATTRIBUTES)
                CommentStore.ItemType.NEW -> presentation.addText(
                    "New comment",
                    SimpleTextAttributes.REGULAR_ATTRIBUTES
                )
                CommentStore.ItemType.REPLY -> presentation.addText("Replying", SimpleTextAttributes.REGULAR_ATTRIBUTES)
            }
        }

        override fun getElement(): CommentStore.Item = item
    }

    private class GroupNode(
        private val ideaProject: IdeaProject,
        private val repository: GitRepository?,
        val data: GroupedComments
    ) : PresentableNodeDescriptor<GroupedComments>(ideaProject, null) {
        override fun update(presentation: PresentationData) {
            if (data.nodeData.isGeneral) {
                presentation.addText(data.nodeData.fileName, SimpleTextAttributes.REGULAR_ATTRIBUTES)
            } else {
                presentation.setIcon(findIcon())
                presentation.addText(data.nodeData.fileName, SimpleTextAttributes.REGULAR_ATTRIBUTES)
                presentation.addText(":${data.nodeData.line}", SimpleTextAttributes.GRAYED_ATTRIBUTES)
                presentation.addText(" (${data.nodeData.fullPath})", SimpleTextAttributes.GRAYED_ATTRIBUTES)
            }
        }

        fun findVirtualFileByPath(path: String): VirtualFile? {
            val file = LocalFileSystem.getInstance().findFileByPath(path)
            if (null !== file) {
                return file
            }
            return null
        }

        fun findPsiFile(ideaProject: IdeaProject, path: String): PsiFile? {
            val file = findVirtualFileByPath(path)
            if (null === file) {
                return null
            }
            return PsiManager.getInstance(ideaProject).findFile(file)
        }

        private fun findIcon(): Icon {
            val psiFile = findPsiFile(ideaProject, RepositoryUtil.findAbsolutePath(repository, data.nodeData.fullPath))
            if (null === psiFile) {
                return AllIcons.FileTypes.Any_type
            }
            return try {
                psiFile.getIcon(Iconable.ICON_FLAG_READ_STATUS)
            } catch (exception: Exception) {
                AllIcons.FileTypes.Any_type
            }
        }

        override fun getElement(): GroupedComments = data
    }
}