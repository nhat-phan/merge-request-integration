package net.ntworld.mergeRequest.api

interface ApiCredentials {
    val url: String

    val ignoreSSLCertificateErrors: Boolean

    val login: String

    val token: String

    val projectId: String

    val version: String

    val info: String
}