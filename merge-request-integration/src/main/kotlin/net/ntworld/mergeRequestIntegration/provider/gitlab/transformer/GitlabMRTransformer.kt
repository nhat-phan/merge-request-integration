package net.ntworld.mergeRequestIntegration.provider.gitlab.transformer

import net.ntworld.mergeRequest.MergeRequest
import net.ntworld.mergeRequest.MergeRequestState
import net.ntworld.mergeRequestIntegration.internal.MergeRequestImpl
import net.ntworld.mergeRequestIntegration.provider.Transformer
import net.ntworld.mergeRequestIntegration.provider.gitlab.*
import net.ntworld.mergeRequestIntegration.util.DateTimeUtil
import org.gitlab4j.api.models.MergeRequest as MergeRequestModel

object GitlabMRTransformer :
    Transformer<MergeRequestModel, MergeRequest> {
    override fun transform(input: MergeRequestModel): MergeRequest = MergeRequestImpl(
        id = input.iid.toString(),
        provider = Gitlab.id,
        projectId = input.projectId.toString(),
        title = input.title,
        description = input.description,
        url = input.webUrl,
        state = when (input.state) {
            MERGE_REQUEST_STATE_OPENED -> MergeRequestState.OPENED
            MERGE_REQUEST_STATE_MERGED -> MergeRequestState.MERGED
            MERGE_REQUEST_STATE_CLOSED -> MergeRequestState.CLOSED
            else -> MergeRequestState.CLOSED
        },
        createdAt = DateTimeUtil.fromDate(input.createdAt),
        updatedAt = DateTimeUtil.fromDate(input.updatedAt),
        assignee = if (null !== input.assignee) GitlabMemberTransformer.transform(input.assignee) else null,
        author = GitlabMemberTransformer.transform(input.author),
        diffReference = GitlabDiffRefTransformer.transform(input.diffRefs),
        sourceBranch = input.sourceBranch,
        targetBranch = input.targetBranch,
        upVotes = input.upvotes,
        downVotes = input.downvotes,
        commentsCount = input.userNotesCount,
        isWorkInProgress = input.workInProgress,
        canMerged = input.mergeStatus == MERGE_STATUS_CAN_BE_MERGED,
        mergedBy = if (null !== input.mergedBy) GitlabMemberTransformer.transform(input.mergedBy) else null,
        closedBy = if (null !== input.closedBy) GitlabMemberTransformer.transform(input.closedBy) else null,
        mergedAt = if (null !== input.mergedAt) DateTimeUtil.fromDate(input.mergedAt) else null,
        closedAt = if (null !== input.closedAt) DateTimeUtil.fromDate(input.closedAt) else null
    )
}