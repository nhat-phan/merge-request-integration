package net.ntworld.mergeRequestIntegrationIde.ui.configuration

import com.intellij.openapi.project.Project
import net.ntworld.mergeRequest.ProviderInfo
import net.ntworld.mergeRequest.api.ApiConnection
import net.ntworld.mergeRequest.api.ApiCredentials
import net.ntworld.mergeRequestIntegration.provider.gitlab.Gitlab
import net.ntworld.mergeRequestIntegration.provider.gitlab.request.GitlabSearchProjectsRequest
import net.ntworld.mergeRequestIntegrationIde.exception.InvalidConnectionException
import net.ntworld.mergeRequestIntegrationIde.internal.ApiCredentialsImpl
import net.ntworld.mergeRequestIntegrationIde.service.ApplicationService

open class GitlabConnectionsConfigurableBase(
    private val ideaProject: Project
) : AbstractConnectionsConfigurable(ideaProject) {
    override fun makeProviderInfo(): ProviderInfo = Gitlab

    override fun findNameFromId(id: String): String {
        if (id.startsWith(PREFIX)) {
            return id.substring(PREFIX.length)
        }
        return id
    }

    override fun findIdFromName(name: String): String {
        return "$PREFIX$name"
    }

    override fun makeConnection(): ConnectionUI {
        return GitlabConnection(ideaProject)
    }

    override fun validateConnection(connection: ApiConnection): Boolean {
        return connection.url.isNotEmpty() && connection.token.isNotEmpty()
    }

    override fun assertConnectionIsValid(connection: ApiConnection) {
        val out = ApplicationService.instance.infrastructure.serviceBus() process GitlabSearchProjectsRequest(
            credentials = ApiCredentialsImpl(
                url = connection.url,
                login = connection.login,
                token = connection.token,
                ignoreSSLCertificateErrors = connection.ignoreSSLCertificateErrors,
                info = "",
                projectId = "",
                version = ""
            ),
            term = ""
        )

        val error = out.getResponse().error
        if (null !== error) {
            throw InvalidConnectionException(error.message)
        }
    }

    override fun getId(): String = "MRI:gitlab"

    override fun getDisplayName(): String = "Gitlab new"

    companion object {
        private const val PREFIX = "gitlab:"
    }
}