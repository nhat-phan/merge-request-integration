package net.ntworld.mergeRequestIntegration.provider.gitlab

import net.ntworld.foundation.Error

data class GitlabFailedRequestError(
    override val message: String,
    override val code: Int
): Error {
    override val type: String = "net.ntworld.mergeRequestIntegration.provider.gitlab.GitlabFailedRequestError"
}