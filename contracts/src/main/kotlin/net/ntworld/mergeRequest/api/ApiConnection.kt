package net.ntworld.mergeRequest.api

interface ApiConnection {
    val url: String

    val ignoreSSLCertificateErrors: Boolean

    val login: String

    val token: String
}