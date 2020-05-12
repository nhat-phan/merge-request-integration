package net.ntworld.mergeRequestIntegration

import net.ntworld.foundation.Infrastructure
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequest.ProviderInfo
import net.ntworld.mergeRequest.api.ApiCredentials
import net.ntworld.mergeRequest.api.ApiOptions
import net.ntworld.mergeRequest.api.ApiProvider

interface ProviderStorage {

    val registeredProviders: List<ProviderData>

    fun updateApiOptions(options: ApiOptions)

    fun register(
        infrastructure: Infrastructure,
        id: String,
        key: String,
        name: String,
        info: ProviderInfo,
        credentials: ApiCredentials,
        repository: String
    ): Pair<ProviderData, Throwable?>

    fun clear()

    fun findOrFail(id: String): Pair<ProviderData, ApiProvider>

}