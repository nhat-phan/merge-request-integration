package net.ntworld.mergeRequestIntegrationIde.internal

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.list
import kotlinx.serialization.serializer
import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequest.CommentPosition
import net.ntworld.mergeRequest.MergeRequest
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequestIntegrationIde.service.CodeReviewUtil
import net.ntworld.mergeRequestIntegrationIde.service.CommentStore
import org.jdom.Element

class CommentStoreItem private constructor() : CommentStore.Item {
    override val id: String
        get() {
            return when (type) {
                CommentStore.ItemType.NEW -> {
                    "$projectId:$mergeRequestId:$typeAsString:${groupNodePath.joinToString(">")}"
                }
                CommentStore.ItemType.EDIT, CommentStore.ItemType.REPLY -> {
                    "$projectId:$mergeRequestId:$typeAsString:$commentId"
                }
            }
        }

    override var type: CommentStore.ItemType = CommentStore.ItemType.NEW
        private set

    override var projectId: String = ""
        private set

    override var mergeRequestId: String = ""
        private set

    override var commentId: String = ""
        private set

    override var commentParentId: String = ""
        private set

    override var groupNodePath: List<String> = listOf()
        private set

    override var position: CommentPosition? = null
        private set

    override var body: String = ""
        private set

    private val typeAsString: String
        get() {
            return when (type) {
                CommentStore.ItemType.EDIT -> "edit"
                CommentStore.ItemType.NEW -> "new"
                CommentStore.ItemType.REPLY -> "reply"
            }
        }

    fun writeTo(parent: Element) {
        val item = Element("Item")
        item.setAttribute("id", id)
        item.setAttribute("type", typeAsString)
        item.setAttribute("projectId", projectId)
        item.setAttribute("mergeRequestId", mergeRequestId)
        item.setAttribute("commentId", commentId)
        item.setAttribute("commentParentId", commentParentId)
        item.setAttribute("groupNodePath", json.stringify(String.serializer().list, groupNodePath))
        val positionData = this.position
        if (null !== positionData) {
            item.setAttribute("hasPosition", "1")
            item.setAttribute("baseHash", positionData.baseHash)
            item.setAttribute("startHash", positionData.startHash)
            item.setAttribute("headHash", positionData.headHash)
            item.setAttribute("oldPath", positionData.oldPath ?: "")
            item.setAttribute("newPath", positionData.newPath ?: "")
            item.setAttribute("oldLine", if (null !== positionData.oldLine) positionData.oldLine.toString() else "")
            item.setAttribute("newLine", if (null !== positionData.newLine) positionData.newLine.toString() else "")
        } else {
            item.setAttribute("hasPosition", "0")
        }
        item.setAttribute("body", body)
        parent.addContent(item)
    }

    companion object {
        private val json = Json(JsonConfiguration.Stable.copy(strictMode = false))

        fun createNewItem(
            providerData: ProviderData,
            mergeRequest: MergeRequest,
            nodeData: CodeReviewUtil.CommentNodeData,
            commentPosition: CommentPosition
        ): CommentStore.Item {
            val instance = CommentStoreItem()
            instance.type = CommentStore.ItemType.NEW
            instance.groupNodePath = listOf(nodeData.getHash())
            instance.projectId = providerData.project.id
            instance.mergeRequestId = mergeRequest.id
            instance.position = commentPosition

            return instance
        }

        fun createNewGeneralItem(
            providerData: ProviderData,
            mergeRequest: MergeRequest,
            nodeData: CodeReviewUtil.CommentNodeData
        ): CommentStore.Item {
            val instance = CommentStoreItem()
            instance.type = CommentStore.ItemType.NEW
            instance.groupNodePath = listOf(nodeData.getHash())
            instance.projectId = providerData.project.id
            instance.mergeRequestId = mergeRequest.id
            instance.position = null

            return instance
        }

        fun createReplyItem(
            providerData: ProviderData,
            mergeRequest: MergeRequest,
            nodeData: CodeReviewUtil.CommentNodeData,
            comment: Comment
        ): CommentStore.Item {
            val instance = CommentStoreItem()
            instance.type = CommentStore.ItemType.REPLY
            instance.groupNodePath = listOf(nodeData.getHash())
            instance.projectId = providerData.project.id
            instance.mergeRequestId = mergeRequest.id
            instance.commentId = comment.id

            return instance
        }
    }
}