package net.ntworld.mergeRequestIntegrationIde.ui.configuration

import com.intellij.openapi.project.Project
import net.ntworld.mergeRequest.ProviderInfo
import net.ntworld.mergeRequest.api.ApiConnection
import net.ntworld.mergeRequestIntegration.provider.gitlab.Gitlab

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
        return GitlabConnection()
    }

    override fun validateConnection(connection: ApiConnection): Boolean {
        return connection.url.isNotEmpty() && connection.token.isNotEmpty()
    }

    override fun getId(): String = "MRI:gitlab"

    override fun getDisplayName(): String = "Gitlab new"

    companion object {
        private const val PREFIX = "gitlab:"
    }
}