package net.ntworld.mergeRequestIntegrationIde.internal

import net.ntworld.mergeRequest.api.ApiCredentials

data class ApiCredentialsImpl(
    override val url: String,
    override val login: String,
    override val token: String,
    override val projectId: String,
    override val version: String,
    override val info: String,
    override val ignoreSSLCertificateErrors: Boolean
): ApiCredentials
