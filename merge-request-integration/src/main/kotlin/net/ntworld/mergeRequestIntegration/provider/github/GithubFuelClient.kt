package net.ntworld.mergeRequestIntegration.provider.github

import com.github.kittinunf.fuel.core.HttpException
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.extensions.authentication
import net.ntworld.foundation.Error
import net.ntworld.foundation.Response
import net.ntworld.mergeRequest.api.ApiCredentials
import net.ntworld.mergeRequestIntegration.provider.FuelClient

class GithubFuelClient private constructor(
    private val credentials: ApiCredentials
) : FuelClient(credentials) {
    val searchIssuesUrl = "${credentials.url}/search/issues"

    override fun injectAuthentication(httpRequest: Request): Request {
        return httpRequest.authentication().basic(credentials.login, credentials.token)
    }

    companion object {
        operator fun <T, R : Response> invoke(
            request: T,
            execute: (GithubFuelClient.(T) -> R),
            failed: ((Error) -> R)
        ): R where T : net.ntworld.foundation.Request<R>, T : GithubRequest {
            return try {
                val client = GithubFuelClient(request.credentials)
                execute.invoke(client, request)
            } catch (exception: HttpException) {
                failed.invoke(
                    GithubFailedRequestError(
                        exception.message ?: "Failed request",
                        500
                    )
                )
            }
        }
    }
}