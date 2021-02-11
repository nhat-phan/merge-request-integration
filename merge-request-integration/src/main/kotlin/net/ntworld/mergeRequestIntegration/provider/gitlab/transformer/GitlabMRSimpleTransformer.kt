package net.ntworld.mergeRequestIntegration.provider.gitlab.transformer

import net.ntworld.mergeRequest.MergeRequestInfo
import net.ntworld.mergeRequest.MergeRequestState
import net.ntworld.mergeRequestIntegration.internal.MergeRequestInfoImpl
import net.ntworld.mergeRequestIntegration.provider.Transformer
import net.ntworld.mergeRequestIntegration.provider.gitlab.*
import net.ntworld.mergeRequestIntegration.util.DateTimeUtil
import org.gitlab4j.api.models.MergeRequest

object GitlabMRSimpleTransformer :
    Transformer<MergeRequest, MergeRequestInfo> {
    override fun transform(input: MergeRequest): MergeRequestInfo = MergeRequestInfoImpl(
        id = input.iid.toString(),
        provider = Gitlab.id,
        projectId = input.projectId.toString(),
        title = input.title,
        description = input.description ?: "",
        url = input.webUrl,
        state = when (input.state) {
            MERGE_REQUEST_STATE_OPENED -> MergeRequestState.OPENED
            MERGE_REQUEST_STATE_MERGED -> MergeRequestState.MERGED
            MERGE_REQUEST_STATE_CLOSED -> MergeRequestState.CLOSED
            else -> MergeRequestState.CLOSED
        },
        createdAt = DateTimeUtil.fromDate(input.createdAt),
        updatedAt = DateTimeUtil.fromDate(input.updatedAt)
    )
}
