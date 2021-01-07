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
    private val data = mutableMapOf<String, MutableMap<String, Comment>>()

    private fun getMutableMap(project: Project, mergeRequestId: String): MutableMap<String, Comment> {
        val key = project.id + "-" + mergeRequestId
        if (!data.contains(key)) {
            data[key] = mutableMapOf()
        }
        return data[key] as MutableMap<String, Comment>
    }

    override fun getAll(project: Project, mergeRequestId: String): List<Comment> {
        return getMutableMap(project, mergeRequestId).values.toList()
    }

    override fun create(project: Project, mergeRequestId: String, body: String, position: CommentPosition?): String? {
        val mutableMap = getMutableMap(project, mergeRequestId)
        val id = "tmp:${UUID.randomUUID()}"
        mutableMap[id] = CommentImpl(
            id = id,
            parentId = "",
            replyId = "",
            body = body,
            position = position,
            createdAt = DateTimeUtil.fromDate(DateTime.now().toDate()),
            updatedAt = DateTimeUtil.fromDate(DateTime.now().toDate()),
            resolvable = false,
            resolved = false,
            resolvedBy = null,
            author = currentUser,
            isDraft = true
        )
        return id
    }

    override fun update(project: Project, mergeRequestId: String, comment: Comment, body: String) {
        if (!comment.isDraft) {
            return
        }
        val mutableMap = getMutableMap(project, mergeRequestId)
        if (mutableMap.contains(comment.id)) {
            mutableMap[comment.id] = CommentImpl(
                id = comment.id,
                parentId = "",
                replyId = "",
                body = body,
                position = comment.position,
                createdAt = comment.createdAt,
                updatedAt = DateTimeUtil.fromDate(DateTime.now().toDate()),
                resolvable = false,
                resolved = false,
                resolvedBy = null,
                author = currentUser,
                isDraft = true
            )
        }
    }

    override fun delete(project: Project, mergeRequestId: String, comment: Comment) {
        if (!comment.isDraft) {
            return
        }
        val mutableMap = getMutableMap(project, mergeRequestId)
        mutableMap.remove(comment.id)
    }
}