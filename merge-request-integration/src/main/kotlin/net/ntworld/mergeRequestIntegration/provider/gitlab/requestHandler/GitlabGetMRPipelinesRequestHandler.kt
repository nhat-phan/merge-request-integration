package net.ntworld.mergeRequestIntegration.provider.gitlab.requestHandler

import kotlinx.serialization.list
import net.ntworld.foundation.Handler
import net.ntworld.foundation.RequestHandler
import net.ntworld.mergeRequestIntegration.provider.gitlab.GitlabFuelClient
import net.ntworld.mergeRequestIntegration.provider.gitlab.model.PipelineModel
import net.ntworld.mergeRequestIntegration.provider.gitlab.request.GitlabGetMRPipelinesRequest
import net.ntworld.mergeRequestIntegration.provider.gitlab.response.GitlabGetMRPipelinesResponse

@Handler
class GitlabGetMRPipelinesRequestHandler : RequestHandler<GitlabGetMRPipelinesRequest, GitlabGetMRPipelinesResponse> {
    override fun handle(request: GitlabGetMRPipelinesRequest): GitlabGetMRPipelinesResponse = GitlabFuelClient(
        request = request,
        execute = {
            val response = this.getJson(
                "${this.baseProjectUrl}/merge_requests/${request.mergeRequestInternalId}/pipelines"
            )

            GitlabGetMRPipelinesResponse(
                error = null,
                pipelines = this.json.parse(PipelineModel.serializer().list, response)
            )
        },
        failed = {
            GitlabGetMRPipelinesResponse(error = it, pipelines = listOf())
        }
    )
}