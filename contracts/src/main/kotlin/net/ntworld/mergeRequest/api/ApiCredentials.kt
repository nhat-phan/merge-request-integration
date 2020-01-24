package net.ntworld.mergeRequest.api

interface ApiCredentials : ApiConnection {
    val projectId: String

    val version: String

    val info: String
}
