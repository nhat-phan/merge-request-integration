package net.ntworld.mergeRequestIntegrationIde.internal

import net.ntworld.mergeRequestIntegrationIde.service.CommentStore

class CommentStoreImpl : CommentStore {
    private val myItems = mutableMapOf<String, CommentStore.Item>()

    override fun getNewItems(): List<CommentStore.Item> {
        return myItems.values.filter {
            it.type == CommentStore.ItemType.NEW
        }
    }

    override fun findNewItem(parentPath: String): CommentStore.Item? {
        for (item in myItems.values) {
            if (item.type == CommentStore.ItemType.NEW && item.groupNodePath.contains(parentPath)) {
                return item
            }
        }
        return null
    }

    override fun findReplyItem(parentPath: String): CommentStore.Item? {
        for (item in myItems.values) {
            if (item.type == CommentStore.ItemType.REPLY && item.groupNodePath.contains(parentPath)) {
                return item
            }
        }
        return null
    }

    override fun add(item: CommentStore.Item) {
        myItems[item.id] = item
    }

    override fun remove(id: String) {
        myItems.remove(id)
    }
}