package net.ntworld.mergeRequestIntegration.provider.github

import net.ntworld.foundation.Error

data class GithubFailedRequestError(
    override val message: String,
    override val code: Int
): Error {
    override val type: String = "net.ntworld.mergeRequestIntegration.provider.github.GithubFailedRequestError"
}