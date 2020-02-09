package net.ntworld.mergeRequestIntegration.provider.gitlab

import net.ntworld.foundation.Infrastructure
import net.ntworld.mergeRequest.User
import net.ntworld.mergeRequest.api.ApiCredentials
import net.ntworld.mergeRequest.api.UserApi
import net.ntworld.mergeRequestIntegration.provider.gitlab.request.GitlabFindCurrentUserRequest
import net.ntworld.mergeRequestIntegration.provider.gitlab.transformer.GitlabUserTransformer

class GitlabUserApi(
    private val infrastructure: Infrastructure,
    private val credentials: ApiCredentials
) : UserApi {
    // override fun find(id: String): UserInfo {
    //     val response = infrastructure.serviceBus() process GitlabFindCurrentUserRequest(credentials) ifError {
    //         throw Exception(it.message)
    //     }
    //
    //     return GitlabUserInfoTransformer.transform(response.user)
    // }

    override fun me(): User {
        val response = infrastructure.serviceBus() process GitlabFindCurrentUserRequest(credentials) ifError {
            throw Exception("Cannot find info of current user.")
        }
        return GitlabUserTransformer.transform(response.user)
    }
}