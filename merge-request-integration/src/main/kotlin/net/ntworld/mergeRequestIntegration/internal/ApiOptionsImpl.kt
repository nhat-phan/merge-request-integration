package net.ntworld.mergeRequestIntegration.internal

import net.ntworld.mergeRequest.api.ApiOptions

data class ApiOptionsImpl(override val enableRequestCache: Boolean) : ApiOptions {
    companion object {
        val DEFAULT = ApiOptionsImpl(
            enableRequestCache = true
        )
    }
}