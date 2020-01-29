package net.ntworld.mergeRequestIntegrationIde.internal

import com.intellij.credentialStore.CredentialAttributes
import com.intellij.credentialStore.generateServiceName
import com.intellij.ide.passwordSafe.PasswordSafe
import com.intellij.openapi.components.PersistentStateComponent
import net.ntworld.mergeRequest.ProviderInfo
import net.ntworld.mergeRequest.api.ApiCredentials
import net.ntworld.mergeRequestIntegration.provider.gitlab.Gitlab
import net.ntworld.mergeRequestIntegrationIde.service.ProviderSettings
import org.jdom.Element

open class ServiceBase : PersistentStateComponent<Element> {
    protected val myProvidersData = mutableMapOf<String, ProviderSettings>()
    protected val supportedProviders: List<ProviderInfo> = listOf(Gitlab)

    override fun getState(): Element? {
        val element = Element("Provider")
        myProvidersData.values.map {
            val item = Element("Item")
            item.setAttribute("id", it.id)
            item.setAttribute("providerId", it.info.id)
            item.setAttribute("url", it.credentials.url)
            item.setAttribute("login", it.credentials.login)
            item.setAttribute("projectId", it.credentials.projectId)
            item.setAttribute("version", it.credentials.version)
            item.setAttribute("info", it.credentials.info)
            item.setAttribute("ignoreSSLCertificateErrors", if (it.credentials.ignoreSSLCertificateErrors) "1" else "0")
            item.setAttribute("repository", it.repository)
            element.addContent(item)
        }
        return element
    }

    override fun loadState(state: Element) {
        for (item in state.children) {
            if (item.name != "Item") {
                continue
            }

            val info = supportedProviders.firstOrNull { it.id == item.getAttribute("providerId").value }
            if (null === info) {
                continue
            }
            val credentials = ApiCredentialsImpl(
                url = item.getAttribute("url").value,
                login = item.getAttribute("login").value,
                token = "",
                projectId = item.getAttribute("projectId").value,
                version = item.getAttribute("version").value,
                info = item.getAttribute("info").value,
                ignoreSSLCertificateErrors = shouldIgnoreSSLCertificateErrors(item)
            )
            val id = item.getAttribute("id").value
            myProvidersData[id] = ProviderSettingsImpl(
                id = id.trim(),
                info = info,
                credentials = decryptCredentials(info, credentials),
                repository = item.getAttribute("repository").value
            )
        }
    }

    private fun shouldIgnoreSSLCertificateErrors(item: Element): Boolean {
        val attribute = item.getAttribute("ignoreSSLCertificateErrors")
        if (null === attribute) {
            return false
        }
        return attribute.value == "1" || attribute.value.toLowerCase() == "true"
    }

    protected fun encryptCredentials(info: ProviderInfo, credentials: ApiCredentials): ApiCredentials {
        encryptPassword(info, credentials, credentials.token)
        return ApiCredentialsImpl(
            url = credentials.url,
            // -----------------------------------------------------------------
            // Always bind login and token because if we don't the token will be
            // empty if the state not stored to the storage yet.
            // It's safe because the secret is stored on memory only.
            login = credentials.login,
            token = credentials.token,
            // -----------------------------------------------------------------
            projectId = credentials.projectId,
            version = credentials.version,
            info = credentials.info,
            ignoreSSLCertificateErrors = credentials.ignoreSSLCertificateErrors
        )
    }

    protected fun decryptCredentials(info: ProviderInfo, credentials: ApiCredentials): ApiCredentials {
        return ApiCredentialsImpl(
            url = credentials.url,
            // -----------------------------------------------------------------
            // Always bind login and token because if we don't the token will be
            // empty if the state not stored to the storage yet.
            // It's safe because the secret is stored on memory only.
            login = credentials.login,
            token = decryptPassword(info, credentials) ?: credentials.token,
            // -----------------------------------------------------------------
            projectId = credentials.projectId,
            version = credentials.version,
            info = credentials.info,
            ignoreSSLCertificateErrors = credentials.ignoreSSLCertificateErrors
        )
    }

    private fun encryptPassword(info: ProviderInfo, credentials: ApiCredentials, password: String) {
        PasswordSafe.instance.setPassword(makeCredentialAttribute(info, credentials), password)
    }

    private fun decryptPassword(info: ProviderInfo, credentials: ApiCredentials): String? {
        val password = PasswordSafe.instance.getPassword(makeCredentialAttribute(info, credentials))
        if (null === password || password.isEmpty()) {
            // Handle legacy CredentialAttribute
            return PasswordSafe.instance.getPassword(makeLegacyCredentialAttribute(info, credentials))
        }
        return password
    }

    /**
     * For Windows, Intellij is using KeePass which have a 36 chars limitation on the group name, therefore I have
     * to shorten the group name since v2019.3.3
     */
    private fun makeCredentialAttribute(info: ProviderInfo, credentials: ApiCredentials): CredentialAttributes {
        if (credentials.url == credentials.login) {
            return CredentialAttributes("MRI:${info.id}", credentials.url)
        }
        return CredentialAttributes("MRI:${info.id}", "${credentials.login}:${credentials.url}")
    }

    /**
     * I have to keep legacy credential attribute otherwise current users have to input the token again
     * which is not available anymore. I meant can't see the token again after refreshing Gitlab's page.
     */
    private fun makeLegacyCredentialAttribute(info: ProviderInfo, credentials: ApiCredentials): CredentialAttributes {
        return CredentialAttributes(
            "MRI - ${info.id} - ${credentials.url} - ${credentials.login}"
        )
    }
}