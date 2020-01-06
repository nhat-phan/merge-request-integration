package net.ntworld.mergeRequestIntegration.provider.gitlab

import net.ntworld.foundation.Error

class GitlabFailedRequestException(error: Error) : Throwable()