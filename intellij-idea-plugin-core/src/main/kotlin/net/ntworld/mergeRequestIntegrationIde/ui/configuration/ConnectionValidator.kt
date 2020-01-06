package net.ntworld.mergeRequestIntegrationIde.ui.configuration

import net.ntworld.mergeRequestIntegration.provider.gitlab.Gitlab
import net.ntworld.mergeRequestIntegrationIde.exception.NotSupportedProviderException
import net.ntworld.mergeRequestIntegrationIde.service.ProviderSettings

object ConnectionValidator {

    fun isValidForTesting(list: List<ProviderSettings?>, index: Int, settings: ProviderSettings): Boolean {
        if (!validateUniqueName(list, index, settings)) {
            return false
        }
        return when (settings.info) {
            is Gitlab -> {
                settings.credentials.url.isNotEmpty() && settings.credentials.token.isNotEmpty()
            }

            else -> throw NotSupportedProviderException(settings.info.id, settings.info.name)
        }
    }

    private fun validateUniqueName(list: List<ProviderSettings?>, index: Int, settings: ProviderSettings): Boolean {
        for (i in 0..list.lastIndex) {
            if (i == index || null === list[i]) {
                continue
            }
            if (list[i]!!.id == settings.id) {
                return false
            }
        }
        return true
    }
}