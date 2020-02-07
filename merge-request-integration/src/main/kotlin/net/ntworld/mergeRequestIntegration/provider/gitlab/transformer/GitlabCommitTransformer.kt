package net.ntworld.mergeRequestIntegration.provider.gitlab.transformer

import net.ntworld.mergeRequest.Commit
import net.ntworld.mergeRequestIntegration.internal.CommitImpl
import net.ntworld.mergeRequestIntegration.provider.Transformer
import net.ntworld.mergeRequestIntegration.util.DateTimeUtil
import org.gitlab4j.api.models.Commit as CommitModel

object GitlabCommitTransformer :
    Transformer<CommitModel, Commit> {
    override fun transform(input: CommitModel): Commit = CommitImpl(
        id = input.id,
        message = input.message,
        authorEmail = input.authorEmail,
        authorName = input.authorName,
        createdAt = DateTimeUtil.fromDate(input.createdAt)
    )
}