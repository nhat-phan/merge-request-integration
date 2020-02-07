package net.ntworld.mergeRequestIntegration.provider.github

import net.ntworld.foundation.Error
import net.ntworld.foundation.Request
import net.ntworld.foundation.Response
import net.ntworld.mergeRequest.api.ApiCredentials
import org.kohsuke.github.GitHub
import org.kohsuke.github.GitHubBuilder
import java.io.IOException

object GithubClient {
    private fun makeGitHub(credentials: ApiCredentials) : GitHub {
        return GitHubBuilder()
            .withEndpoint(credentials.url)
            .withPassword(credentials.login, credentials.token)
            .build()
    }

    operator fun <T, R: Response> invoke(
        request: T,
        execute: (GitHub.(T) -> R),
        failed: ((Error) -> R)
    ) : R where T : Request<R>, T : GithubRequest {
        return try {
            execute.invoke(
                makeGitHub(request.credentials),
                request
            )
        } catch (exception: IOException) {
            failed.invoke(
                GithubFailedRequestError(
                    exception.message ?: "Failed request",
                    400
                )
            )
        }
    }
}