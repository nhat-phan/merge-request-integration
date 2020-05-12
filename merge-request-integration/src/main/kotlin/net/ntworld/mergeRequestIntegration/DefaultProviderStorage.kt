package net.ntworld.mergeRequestIntegration

import net.ntworld.foundation.Infrastructure
import net.ntworld.mergeRequest.*
import net.ntworld.mergeRequest.api.ApiCredentials
import net.ntworld.mergeRequest.api.ApiOptions
import net.ntworld.mergeRequest.api.ApiProvider
import net.ntworld.mergeRequestIntegration.exception.ProviderNotFoundException
import net.ntworld.mergeRequestIntegration.internal.ProjectImpl
import net.ntworld.mergeRequestIntegration.internal.ProviderDataImpl
import net.ntworld.mergeRequestIntegration.internal.UserImpl
import net.ntworld.mergeRequestIntegration.provider.MemoryCache
import net.ntworld.mergeRequestIntegration.provider.gitlab.Gitlab
import net.ntworld.mergeRequestIntegration.provider.gitlab.GitlabApiProvider
import java.util.*

class DefaultProviderStorage : ProviderStorage {
    private val data = Collections.synchronizedMap(mutableMapOf<String, ProviderData>())
    private val api = Collections.synchronizedMap(mutableMapOf<String, ApiProvider>())

    override val registeredProviders
        get() = data.values.toList()

    override fun updateApiOptions(options: ApiOptions) {
        api.forEach { it.value.setOptions(options) }
    }

    override fun register(
        infrastructure: Infrastructure,
        id: String,
        key: String,
        name: String,
        info: ProviderInfo,
        credentials: ApiCredentials,
        repository: String
    ): Pair<ProviderData, Throwable?> {
        val api = createApiProvider(infrastructure = infrastructure, id = id, info = info, credentials = credentials)
        var throwable: Throwable? = null
        var message: String? = null
        try {
            val user = api.user.me()
            val project = api.project.find(credentials.projectId)
            if (null !== project) {
                val providerData = ProviderDataImpl(
                    id = id,
                    key = key,
                    name = name,
                    info = info,
                    credentials = credentials,
                    project = project,
                    currentUser = user,
                    repository = repository,
                    errorMessage = null,
                    status = ProviderStatus.ACTIVE
                )
                data[id] = providerData
                return Pair(providerData, null)
            }
        } catch (exception: Throwable) {
            message = exception.message
            throwable = exception
        }
        val invalid = ProviderDataImpl(
            id = id,
            key = key,
            name = name,
            info = info,
            credentials = credentials,
            project = ProjectImpl("", info, "", "", "", "", ProjectVisibility.PUBLIC, "", ""),
            currentUser = UserImpl("", "[Error]", "<error>", "", "", UserStatus.INACTIVE, "", ""),
            repository = repository,
            errorMessage = message,
            status = ProviderStatus.ERROR
        )
        data[id] = invalid
        return Pair(invalid, throwable)
    }

    override fun clear() {
        data.clear()
        api.clear()
    }

    private fun createApiProvider(
        infrastructure: Infrastructure,
        id: String,
        info: ProviderInfo,
        credentials: ApiCredentials
    ): ApiProvider {
        val created = when (info.id) {
            Gitlab.id -> GitlabApiProvider(
                infrastructure = infrastructure,
                credentials = credentials,
                cache = MemoryCache()
            )
            // Github.id -> GithubApiProvider(
            //     infrastructure = infrastructure,
            //     credentials = credentials,
            //     cache = MemoryCache()
            // )
            else -> throw Exception("Cannot create ApiProvider ${info.id}")
        }
        api[id] = created
        return created
    }

    override fun findOrFail(id: String): Pair<ProviderData, ApiProvider> {
        return Pair(findDataOrFail(id), findProviderOrFail(id))
    }

    private fun findDataOrFail(id: String): ProviderData {
        val item = data[id]
        return if (null !== item) {
            item
        } else {
            throw ProviderNotFoundException()
        }
    }

    private fun findProviderOrFail(id: String): ApiProvider {
        val provider = api[id]
        return if (null !== provider) {
            provider
        } else {
            throw ProviderNotFoundException()
        }
    }
}