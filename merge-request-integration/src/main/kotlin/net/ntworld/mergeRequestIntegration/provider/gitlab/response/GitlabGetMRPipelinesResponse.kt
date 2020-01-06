package net.ntworld.mergeRequestIntegration.provider.gitlab.response

import net.ntworld.foundation.Error
import net.ntworld.foundation.Response
import net.ntworld.mergeRequestIntegration.provider.gitlab.model.PipelineModel

data class GitlabGetMRPipelinesResponse(
    override val error: Error?,
    val pipelines: List<PipelineModel>
) : Response
