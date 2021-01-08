package net.ntworld.mergeRequestIntegrationIde.mergeRequest.comments.tree.node

import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.util.treeView.PresentableNodeDescriptor
import com.intellij.openapi.command.impl.DummyProject
import net.ntworld.mergeRequest.*
import net.ntworld.mergeRequest.generated.CommentImpl
import net.ntworld.mergeRequest.generated.CommentPositionImpl
import net.ntworld.mergeRequestIntegration.internal.UserImpl
import javax.swing.tree.DefaultMutableTreeNode
import kotlin.test.Test
import kotlin.test.assertEquals

class NodeSyncManagerImplTest {
    private val user = UserImpl(
        id = "1", name = "name", username = "username", email = "email",
        avatarUrl = "", url = "", status = UserStatus.ACTIVE, createdAt = ""
    )
    private val commentPosition = CommentPositionImpl(
        "", "", "", null, null, null, null, CommentPositionSource.UNKNOWN, CommentPositionChangeType.UNKNOWN
    )

    @Test
    fun `testSyncStructure can generate correct structure with empty tree`() {
        val service = makeService()
        val root = generateFullList()
        val rootTreeNode = DefaultMutableTreeNode()

        service.syncStructure(root, rootTreeNode, DEFAULT_INVOKER)

        assertEquals("""
            --root
            ----general-comments
            ------thread[1]
            --------comment[2]
            ------thread[3]
            ----file[/dir/file-1]
            ------line[/dir/file-1:1]
            --------thread[4]
            ------line[/dir/file-1:2]
            --------thread[5]
            ----------comment[6]
            ----------comment[7]
            --------thread[8]
            ----------comment[9]
            ----------comment[10]
            ----------comment[11]
            ----file[/file-2]
            ------line[/file-2:3]
            --------thread[12]
            !END
        """.trimIndent(), generateStructureFootprint(rootTreeNode))
    }

    @Test
    fun `testSyncStructure can add more node into current structure`() {
        val service = makeService()
        val root = generateFullList()
        val rootTreeNode = DefaultMutableTreeNode()
        service.syncStructure(root, rootTreeNode, DEFAULT_INVOKER)

        root.children[1].children[1].children[0].add(CommentNode(makeComment("Insert", "new"), null))
        service.syncStructure(root, rootTreeNode, DEFAULT_INVOKER)

        assertEquals("""
            --root
            ----general-comments
            ------thread[1]
            --------comment[2]
            ------thread[3]
            ----file[/dir/file-1]
            ------line[/dir/file-1:1]
            --------thread[4]
            ------line[/dir/file-1:2]
            --------thread[5]
            ----------comment[6]
            ----------comment[7]
            ----------comment[Insert]
            --------thread[8]
            ----------comment[9]
            ----------comment[10]
            ----------comment[11]
            ----file[/file-2]
            ------line[/file-2:3]
            --------thread[12]
            !END
        """.trimIndent(), generateStructureFootprint(rootTreeNode))
    }

    @Test
    fun `testSyncStructure can remove node out of current structure`() {
        val service = makeService()
        val root = generateFullList()
        val rootTreeNode = DefaultMutableTreeNode()
        service.syncStructure(root, rootTreeNode, DEFAULT_INVOKER)

        (root.children[1].children[1].children as MutableList<Node>).removeAt(0)
        root.children.removeAt(0)
        (root.children[0].children[1].children[0].children as MutableList<Node>).removeAt(1)
        service.syncStructure(root, rootTreeNode, DEFAULT_INVOKER)

        assertEquals("""
            --root
            ----file[/dir/file-1]
            ------line[/dir/file-1:1]
            --------thread[4]
            ------line[/dir/file-1:2]
            --------thread[8]
            ----------comment[9]
            ----------comment[11]
            ----file[/file-2]
            ------line[/file-2:3]
            --------thread[12]
            !END
        """.trimIndent(), generateStructureFootprint(rootTreeNode))
    }

    @Test
    fun `testSyncStructure can remove items and delete correct indexes`() {
        val service = makeService()
        val root = generateFullList()
        val rootTreeNode = DefaultMutableTreeNode()
        service.syncStructure(root, rootTreeNode, DEFAULT_INVOKER)

        (root.children[1].children as MutableList<Node>).removeAt(0)
        service.syncStructure(root, rootTreeNode, DEFAULT_INVOKER)

        assertEquals("""
            --root
            ----general-comments
            ------thread[1]
            --------comment[2]
            ------thread[3]
            ----file[/dir/file-1]
            ------line[/dir/file-1:2]
            --------thread[5]
            ----------comment[6]
            ----------comment[7]
            --------thread[8]
            ----------comment[9]
            ----------comment[10]
            ----------comment[11]
            ----file[/file-2]
            ------line[/file-2:3]
            --------thread[12]
            !END
        """.trimIndent(), generateStructureFootprint(rootTreeNode))

        (root.children as MutableList<Node>).removeAt(0)
        service.syncStructure(root, rootTreeNode, DEFAULT_INVOKER)

        assertEquals("""
            --root
            ----file[/dir/file-1]
            ------line[/dir/file-1:2]
            --------thread[5]
            ----------comment[6]
            ----------comment[7]
            --------thread[8]
            ----------comment[9]
            ----------comment[10]
            ----------comment[11]
            ----file[/file-2]
            ------line[/file-2:3]
            --------thread[12]
            !END
        """.trimIndent(), generateStructureFootprint(rootTreeNode))
    }

    private fun generateStructureFootprint(treeNode: DefaultMutableTreeNode): String {
        val stringBuilder = StringBuilder()
        generateStructureFootprint(stringBuilder, treeNode, 0)
        stringBuilder.append("!END")
        return stringBuilder.toString()
    }

    @Suppress("UNCHECKED_CAST")
    private fun generateStructureFootprint(result: StringBuilder, treeNode: DefaultMutableTreeNode, deep: Int) {
        var padding = ""
        for (i in 0..deep) {
            padding += "--"
        }
        val userObject = treeNode.userObject as PresentableNodeDescriptor<Node>
        val node = userObject.element
        result.append("${padding}${node.id}")
        result.appendln()
        val children = treeNode.children()
        while (children.hasMoreElements()) {
            generateStructureFootprint(result, children.nextElement() as DefaultMutableTreeNode, deep+1)
        }
    }

    private fun generateFullList(): RootNode {
        val root = RootNode()

        val generalComments = GeneralCommentsNode(3, 0)

        val threadOne = ThreadNode("thread-one", 1, 0, makeComment("1", "thread-one"), null)
        threadOne.add(CommentNode(makeComment("2", "thread-one"), null))
        generalComments.add(threadOne)

        val threadTwo = ThreadNode("thread-two", 0, 0, makeComment("3", "thread-two"), null)
        generalComments.add(threadTwo)

        root.add(generalComments)

        val fileOne = FileNode("/dir/file-1", 0)
        val line1 = FileLineNode("/dir/file-1", 1, commentPosition, 1, 0, false)
        val threadThree = ThreadNode("thread-three", 0, 0, makeComment("4", "thread-three"), null)
        line1.add(threadThree)

        val line2 = FileLineNode("/dir/file-1", 2, commentPosition, 0, 0, false)
        val threadFour = ThreadNode("thread-four", 2, 0, makeComment("5", "thread-four"), null)
        threadFour.add(CommentNode(makeComment("6", "thread-four"), null))
        threadFour.add(CommentNode(makeComment("7", "thread-four"), null))
        line2.add(threadFour)
        val threadFive = ThreadNode("thread-five", 3, 0, makeComment("8", "thread-four"), null)
        threadFive.add(CommentNode(makeComment("9", "thread-five"), null))
        threadFive.add(CommentNode(makeComment("10", "thread-five"), null))
        threadFive.add(CommentNode(makeComment("11", "thread-five"), null))
        line2.add(threadFive)

        fileOne.add(line1)
        fileOne.add(line2)

        val fileTwo = FileNode("/file-2", 0)
        val line3 = FileLineNode("/file-2", 3, commentPosition, 1, 0, false)
        val threadSix = ThreadNode("thread-six", 0, 0, makeComment("12", "thread-six"), null)
        line3.add(threadSix)
        fileTwo.add(line3)

        root.add(fileOne)
        root.add(fileTwo)

        return root
    }

    private fun makeService(): NodeSyncManagerImpl {
        return NodeSyncManagerImpl(DummyNodeDescriptorService())
    }

    private fun makeComment(id: String, threadId: String): Comment {
        return CommentImpl(
            id = id,
            parentId = threadId,
            replyId = threadId,
            body = "comment $id",
            author = user,
            position = null,
            createdAt = "",
            updatedAt = "",
            resolvable = true,
            resolved = false,
            resolvedBy = null,
            isDraft = true
        )
    }

    private class DummyPresentableNodeDescriptor(
        private val element: Node
    ) : PresentableNodeDescriptor<Node>(DummyProject.getInstance(), null) {
        override fun update(presentation: PresentationData) {
        }
        override fun getElement(): Node = element
    }

    private class DummyNodeDescriptorService: NodeDescriptorService {
        override fun make(node: Node): PresentableNodeDescriptor<Node> {
            return DummyPresentableNodeDescriptor(node)
        }

        override fun findNode(input: Any?): Node? {
            return null
        }

        override fun isHolding(input: Any?, node: Node): Boolean {
            return false
        }
    }

    companion object {
        val DEFAULT_INVOKER: ((Node, DefaultMutableTreeNode) -> Unit) = { _, _ -> }
    }
}