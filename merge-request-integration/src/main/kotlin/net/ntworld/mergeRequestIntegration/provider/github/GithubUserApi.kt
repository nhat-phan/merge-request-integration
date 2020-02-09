package net.ntworld.mergeRequestIntegration.provider.github

import net.ntworld.foundation.Infrastructure
import net.ntworld.mergeRequest.User
import net.ntworld.mergeRequest.api.ApiCredentials
import net.ntworld.mergeRequest.api.UserApi
import net.ntworld.mergeRequestIntegration.provider.github.request.GithubFindCurrentUserRequest
import net.ntworld.mergeRequestIntegration.provider.github.transformer.GithubUserTransformer

class GithubUserApi(
    private val infrastructure: Infrastructure,
    private val credentials: ApiCredentials
) : UserApi {

    override fun me(): User {
        val response = infrastructure.serviceBus() process GithubFindCurrentUserRequest(credentials) ifError {
            throw Exception("Cannot find info of current user.")
        }
        return GithubUserTransformer.transform(response.user)
    }

}