package net.ntworld.mergeRequestIntegration.provider

import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequest.CommentPosition
import net.ntworld.mergeRequest.Project
import net.ntworld.mergeRequest.UserInfo
import net.ntworld.mergeRequest.api.DraftCommentStorage
import net.ntworld.mergeRequestIntegration.internal.CommentImpl
import net.ntworld.mergeRequestIntegration.util.DateTimeUtil
import org.joda.time.DateTime
import java.util.*

class MemoryDraftCommentStorage(private val currentUser: UserInfo) : DraftCommentStorage {
    private val data = mutableMapOf<String, MutableList<Comment>>()

    private fun getMutableList(project: Project, mergeRequestId: String): MutableList<Comment> {
        val key = project.id + "-" + mergeRequestId
        if (!data.contains(key)) {
            data[key] = mutableListOf()
        }
        return data[key] as MutableList<Comment>
    }

    override fun getAll(project: Project, mergeRequestId: String): List<Comment> {
        return getMutableList(project, mergeRequestId)
    }

    override fun create(project: Project, mergeRequestId: String, body: String, position: CommentPosition?): String? {
        val mutableList = getMutableList(project, mergeRequestId)
        val id = "tmp:${UUID.randomUUID()}"
        mutableList.add(CommentImpl(
            id = id,
            parentId = "",
            replyId = "",
            body = body,
            position = position,
            createdAt = DateTimeUtil.formatDate(DateTime.now().toDate()),
            updatedAt = DateTimeUtil.formatDate(DateTime.now().toDate()),
            resolvable = false,
            resolved = false,
            resolvedBy = null,
            author = currentUser,
            isDraft = true
        ))
        return id
    }

    override fun delete(project: Project, mergeRequestId: String, comment: Comment) {
        if (!comment.isDraft) {
            return
        }
        val mutableList = getMutableList(project, mergeRequestId)
        val item = mutableList.find { it.id === comment.id }
        if (null !== item) {
            mutableList.remove(item)
        }
    }
}