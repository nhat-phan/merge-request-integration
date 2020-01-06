package net.ntworld.mergeRequestIntegrationIde.ui.configuration.gitlab

import net.ntworld.mergeRequest.Project
import net.ntworld.mergeRequestIntegration.provider.gitlab.request.GitlabFindProjectRequest
import net.ntworld.mergeRequestIntegration.provider.gitlab.request.GitlabSearchProjectsRequest
import net.ntworld.mergeRequestIntegration.provider.gitlab.transformer.GitlabProjectTransformer
import net.ntworld.mergeRequestIntegrationIde.service.ApplicationService
import net.ntworld.mergeRequestIntegrationIde.service.ProviderSettings

object GitlabConnectionVerifier {

    fun verifyProject(providerSettings: ProviderSettings): Pair<Boolean, Project?> {
        val out = ApplicationService.instance.infrastructure.serviceBus() process GitlabFindProjectRequest(
            credentials = providerSettings.credentials,
            projectId = providerSettings.credentials.projectId
        )
        val response = out.getResponse()
        return if (response.isSuccess) {
            Pair(true, GitlabProjectTransformer.transform(response.project))
        } else {
            Pair(false, null)
        }
    }

    fun verify(providerSettings: ProviderSettings): Pair<Boolean, String> {
        val out = ApplicationService.instance.infrastructure.serviceBus() process GitlabSearchProjectsRequest(
            credentials = providerSettings.credentials,
            term = ""
        )

        val error = out.getResponse().error
        return if (null !== error) {
            Pair(false, error.message)
        } else {
            Pair(true, "")
        }
    }

}