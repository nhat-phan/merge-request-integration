package net.ntworld.mergeRequestIntegration.provider.gitlab.transformer

import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequest.CommentPositionChangeType
import net.ntworld.mergeRequest.CommentPositionSource
import net.ntworld.mergeRequest.UserStatus
import net.ntworld.mergeRequestIntegration.internal.CommentImpl
import net.ntworld.mergeRequestIntegration.internal.CommentPositionImpl
import net.ntworld.mergeRequestIntegration.internal.UserInfoImpl
import net.ntworld.mergeRequestIntegration.provider.Transformer
import net.ntworld.mergeRequestIntegration.util.DateTimeUtil
import org.gitlab4j.api.models.Discussion
import org.gitlab4j.api.models.Note

object GitlabDiscussionTransformer :
    Transformer<Discussion, List<Comment>> {

    override fun transform(input: Discussion): List<Comment> {
        return input.notes.filter { !it.system }.map {
            transformItem(input, it)
        }
    }

    private fun transformItem(discussion: Discussion, input: Note) = CommentImpl(
        id = input.id.toString(),
        parentId = discussion.id,
        replyId = input.id.toString(),
        body = input.body,
        author = UserInfoImpl(
            id = input.author.id.toString(),
            name = input.author.name,
            username = input.author.username,
            url = input.author.webUrl,
            avatarUrl = input.author.avatarUrl,
            status = UserStatus.ACTIVE
        ),
        createdAt = DateTimeUtil.fromDate(input.createdAt),
        updatedAt = DateTimeUtil.fromDate(input.updatedAt),
        resolvedBy = if (null !== input.resolvedBy) {
            UserInfoImpl(
                id = input.resolvedBy.id.toString(),
                name = input.resolvedBy.name,
                username = input.resolvedBy.username,
                url = input.resolvedBy.webUrl,
                avatarUrl = input.resolvedBy.avatarUrl,
                status = UserStatus.ACTIVE
            )
        } else null,
        resolved = if (null === input.resolved) false else input.resolved,
        resolvable = input.resolvable,
        position = if (null !== input.position) {
            CommentPositionImpl(
                startHash = input.position.startSha,
                baseHash = input.position.baseSha,
                headHash = input.position.headSha,
                oldPath = input.position.oldPath,
                newPath = input.position.newPath,
                oldLine = input.position.oldLine,
                newLine = input.position.newLine,
                source = CommentPositionSource.SERVER,
                changeType = CommentPositionChangeType.UNKNOWN
            )
        } else null
    )

}