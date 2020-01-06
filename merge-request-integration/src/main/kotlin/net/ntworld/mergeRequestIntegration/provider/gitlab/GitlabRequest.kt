package net.ntworld.mergeRequestIntegration.provider.gitlab

import net.ntworld.mergeRequest.api.ApiCredentials

interface GitlabRequest {
    val credentials: ApiCredentials
}