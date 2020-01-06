package net.ntworld.mergeRequestIntegration.provider.gitlab.requestHandler

import net.ntworld.foundation.Handler
import net.ntworld.foundation.RequestHandler
import net.ntworld.mergeRequestIntegration.provider.gitlab.GitlabClient
import net.ntworld.mergeRequestIntegration.provider.gitlab.request.GitlabFindProjectRequest
import net.ntworld.mergeRequestIntegration.provider.gitlab.response.GitlabFindProjectResponse
import org.gitlab4j.api.models.Project

@Handler
class GitlabFindProjectRequestHandler : RequestHandler<GitlabFindProjectRequest, GitlabFindProjectResponse> {

    override fun handle(request: GitlabFindProjectRequest): GitlabFindProjectResponse = GitlabClient(
        request = request,
        execute = {
            val project = this.projectApi.getProject(request.projectId.toInt())

            GitlabFindProjectResponse(error = null, project = project)
        },
        failed = {
            GitlabFindProjectResponse(error = it, project = Project())
        }
    )

}
