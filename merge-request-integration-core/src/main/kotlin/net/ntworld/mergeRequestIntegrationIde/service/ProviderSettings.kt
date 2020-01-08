package net.ntworld.mergeRequestIntegrationIde.service

import net.ntworld.mergeRequest.ProviderInfo
import net.ntworld.mergeRequest.api.ApiCredentials

interface ProviderSettings {
    val id: String

    val info: ProviderInfo

    val credentials: ApiCredentials
    
    val repository: String

    val sharable: Boolean
}