package net.ntworld.mergeRequestIntegration.provider.github.transformer

import net.ntworld.mergeRequest.MergeRequestInfo
import net.ntworld.mergeRequest.MergeRequestState
import net.ntworld.mergeRequestIntegration.internal.MergeRequestInfoImpl
import net.ntworld.mergeRequestIntegration.provider.Transformer
import net.ntworld.mergeRequestIntegration.provider.github.Github
import net.ntworld.mergeRequestIntegration.provider.github.PULL_REQUEST_STATE_CLOSED
import net.ntworld.mergeRequestIntegration.provider.github.PULL_REQUEST_STATE_MERGED
import net.ntworld.mergeRequestIntegration.provider.github.PULL_REQUEST_STATE_OPEN
import net.ntworld.mergeRequestIntegration.provider.github.model.PullRequestSearchItem
import net.ntworld.mergeRequestIntegration.provider.github.vo.GithubMergeRequestId

class GithubSearchPullRequestItemTransformer(
    private val projectId: String
) : Transformer<PullRequestSearchItem, MergeRequestInfo> {
    override fun transform(input: PullRequestSearchItem): MergeRequestInfo = MergeRequestInfoImpl(
        id = GithubMergeRequestId(input.id, input.number, input.nodeId).getValue(),
        provider = Github.id,
        projectId = projectId,
        title = input.title,
        description = input.body,
        url = input.htmlUrl,
        state = when (input.state) {
            PULL_REQUEST_STATE_OPEN -> MergeRequestState.OPENED
            PULL_REQUEST_STATE_MERGED -> MergeRequestState.MERGED
            PULL_REQUEST_STATE_CLOSED -> MergeRequestState.CLOSED
            else -> MergeRequestState.CLOSED
        },
        createdAt = input.createdAt,
        updatedAt = input.updatedAt
    )
}
