package net.ntworld.mergeRequestIntegrationIde.internal

import net.ntworld.mergeRequest.ProviderInfo
import net.ntworld.mergeRequest.api.ApiCredentials
import net.ntworld.mergeRequestIntegrationIde.service.ProviderSettings

data class ProviderSettingsImpl(
    override val id: String,
    override val info: ProviderInfo,
    override val credentials: ApiCredentials,
    override val repository: String,
    override val sharable: Boolean = false
) : ProviderSettings {
    companion object {
        fun makeDefault(id: String, info: ProviderInfo): ProviderSettings {
            return ProviderSettingsImpl(
                id = id,
                info = info,
                credentials = ApiCredentialsImpl(
                    url = "",
                    login = "",
                    token = "",
                    projectId = "",
                    version = "",
                    info = "",
                    ignoreSSLCertificateErrors = false
                ),
                repository = "",
                sharable = false
            )
        }

    }
}
