package net.ntworld.mergeRequestIntegration.provider.gitlab

import net.ntworld.foundation.Error
import net.ntworld.foundation.Request
import net.ntworld.foundation.Response
import net.ntworld.mergeRequest.api.ApiCredentials
import net.ntworld.mergeRequestIntegration.provider.gitlab.request.GitlabSearchProjectsRequest
import org.gitlab4j.api.Constants
import org.gitlab4j.api.GitLabApi
import org.gitlab4j.api.GitLabApiException

class GitlabClient {
    companion object {
        private fun makeGitLabApi(credentials: ApiCredentials): GitLabApi {
            val api = GitLabApi(
                credentials.url,
                Constants.TokenType.PRIVATE,
                credentials.token
            )
            if (credentials.ignoreSSLCertificateErrors) {
                api.ignoreCertificateErrors = true
            }
            return api
        }

        operator fun <T, R : Response> invoke(
            request: T,
            execute: (GitLabApi.(T) -> R),
            failed: ((Error) -> R)
        ): R where T : Request<R>, T : GitlabRequest {
            return try {
                execute.invoke(
                    makeGitLabApi(request.credentials),
                    request
                )
            } catch (exception: GitLabApiException) {
                failed.invoke(
                    GitlabFailedRequestError(
                        exception.message ?: "Failed request",
                        exception.httpStatus
                    )
                )
            }
        }

        operator fun <T> invoke(
            credentials: ApiCredentials,
            execute: (GitLabApi.() -> T),
            failed: ((Error) -> T)
        ): T {
            return try {
                execute.invoke(
                    makeGitLabApi(credentials)
                )
            } catch (exception: GitLabApiException) {
                failed.invoke(
                    GitlabFailedRequestError(
                        exception.message ?: "Failed request",
                        exception.httpStatus
                    )
                )
            }
        }
    }
}