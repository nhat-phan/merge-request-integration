package net.ntworld.mergeRequestIntegration.provider.gitlab.transformer

import net.ntworld.mergeRequest.Pipeline
import net.ntworld.mergeRequest.PipelineStatus
import net.ntworld.mergeRequestIntegration.internal.PipelineImpl
import net.ntworld.mergeRequestIntegration.provider.Transformer
import net.ntworld.mergeRequestIntegration.provider.gitlab.*
import net.ntworld.mergeRequestIntegration.provider.gitlab.model.PipelineModel

object GitlabPipelineTransformer :
    Transformer<PipelineModel, Pipeline> {
    override fun transform(input: PipelineModel): Pipeline = PipelineImpl(
        id = input.id.toString(),
        hash = input.sha,
        ref = input.ref,
        status = findStatus(input.status),
        url = input.webUrl,
        createdAt = input.createdAt,
        updatedAt = input.updatedAt
    )

    private fun findStatus(status: String): PipelineStatus {
        if (status == PIPELINE_STATUS_FAILED) {
            return PipelineStatus.FAILED
        }
        if (status == PIPELINE_STATUS_PARTIAL_FAILED) {
            return PipelineStatus.PARTIAL_FAILED
        }
        if (status == PIPELINE_STATUS_SUCCESS) {
            return PipelineStatus.SUCCESS
        }
        if (status == PIPELINE_STATUS_RUNNING) {
            return PipelineStatus.RUNNING
        }
        return PipelineStatus.UNKNOWN
    }
}