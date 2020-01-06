package net.ntworld.mergeRequestIntegration.provider.gitlab.transformer

import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequest.CommentPositionSource
import net.ntworld.mergeRequest.UserStatus
import net.ntworld.mergeRequestIntegration.internal.CommentImpl
import net.ntworld.mergeRequestIntegration.internal.CommentPositionImpl
import net.ntworld.mergeRequestIntegration.internal.UserInfoImpl
import net.ntworld.mergeRequestIntegration.provider.gitlab.Transformer
import net.ntworld.mergeRequestIntegration.provider.gitlab.model.GetCommentsPayload

object GitlabCommentTransformer : Transformer<GetCommentsPayload.Note, Comment> {

    override fun transform(input: GetCommentsPayload.Note): Comment = CommentImpl(
        id = input.id,
        parentId = "",
        replyId = input.id,
        body = input.body,
        author = UserInfoImpl(
            id = "",
            name = input.author.name,
            username = input.author.username,
            url = input.author.webUrl,
            avatarUrl = input.author.avatarUrl,
            status = UserStatus.ACTIVE
        ),
        createdAt = input.createdAt,
        updatedAt = input.updatedAt,
        resolvedBy = if (null !== input.resolvedBy) {
            UserInfoImpl(
                id = "",
                name = input.author.name,
                username = input.author.username,
                url = input.author.webUrl,
                avatarUrl = input.author.avatarUrl,
                status = UserStatus.ACTIVE
            )
        } else null,
        resolved = null !== input.resolvedBy,
        resolvable = input.resolvable,
        position = if (null !== input.position) {
            CommentPositionImpl(
                startHash = input.position.diffRefs.startSha,
                baseHash = input.position.diffRefs.baseSha,
                headHash = input.position.diffRefs.headSha,
                oldPath = input.position.oldPath,
                newPath = input.position.newPath,
                oldLine = input.position.oldLine,
                newLine = input.position.newLine,
                source = CommentPositionSource.UNKNOWN
            )
        } else null
    )

}