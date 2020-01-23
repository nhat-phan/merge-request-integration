package net.ntworld.mergeRequestIntegration.provider.gitlab.requestHandler

import net.ntworld.foundation.Handler
import net.ntworld.foundation.RequestHandler
import net.ntworld.mergeRequestIntegration.provider.gitlab.GitlabClient
import net.ntworld.mergeRequestIntegration.provider.gitlab.request.GitlabSearchProjectsRequest
import net.ntworld.mergeRequestIntegration.provider.gitlab.response.GitlabSearchProjectsResponse
import org.gitlab4j.api.Constants

@Handler
class GitlabSearchProjectsRequestHandler : RequestHandler<GitlabSearchProjectsRequest, GitlabSearchProjectsResponse> {

    override fun handle(request: GitlabSearchProjectsRequest): GitlabSearchProjectsResponse = GitlabClient(
        request = request,
        execute = {
            this.ignoreCertificateErrors = true

            val projects = if (it.term.isEmpty()) {
                this.projectApi.getProjects(10).first()
            } else {
                this.projectApi.getProjects(
                    false,
                    null,
                    Constants.ProjectOrderBy.CREATED_AT,
                    Constants.SortOrder.ASC,
                    request.term,
                    false,
                    request.owner,
                    request.membership,
                    request.starred,
                    false,
                    10
                ).first()
            }

            GitlabSearchProjectsResponse(error = null, projects = projects)
        },
        failed = {
            GitlabSearchProjectsResponse(error = it, projects = listOf())
        }
    )

}
