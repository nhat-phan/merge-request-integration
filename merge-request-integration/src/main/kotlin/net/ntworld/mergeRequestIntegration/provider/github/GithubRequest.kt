package net.ntworld.mergeRequestIntegration.provider.github

import net.ntworld.mergeRequest.api.ApiCredentials

interface GithubRequest {
    val credentials: ApiCredentials
}