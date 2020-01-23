package net.ntworld.mergeRequestIntegration.provider.gitlab

import net.ntworld.mergeRequest.api.ApiCredentials

data class GitlabCredentials(
    override val url: String,

    override val token: String,

    override val projectId: String,

    override val login: String = "",

    override val version: String = "v4",

    override val info: String = "",

    override val ignoreSSLCertificateErrors: Boolean = false
): ApiCredentials {

    companion object {
        val Empty = GitlabCredentials("", "", "", "")
    }
}
