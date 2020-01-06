package net.ntworld.mergeRequestIntegrationIde.service

import net.ntworld.mergeRequest.CommentPosition

interface CommentStore {
    fun getNewItems(): List<CommentStore.Item>

    fun findNewItem(parentPath: String): Item?

    fun findReplyItem(parentPath: String): Item?

    fun add(item: Item)

    fun remove(id: String)

    interface Item {
        val id: String

        val type: ItemType

        val projectId: String

        val mergeRequestId: String

        val commentId: String

        val commentParentId: String

        val groupNodePath: List<String>

        val position: CommentPosition?

        val body: String
    }

    enum class ItemType {
        EDIT,
        NEW,
        REPLY
    }
}
