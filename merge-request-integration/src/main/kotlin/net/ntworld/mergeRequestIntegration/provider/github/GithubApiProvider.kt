package net.ntworld.mergeRequestIntegration.provider.github

import net.ntworld.foundation.Infrastructure
import net.ntworld.mergeRequest.ProviderInfo
import net.ntworld.mergeRequest.api.*

class GithubApiProvider(
    private val infrastructure: Infrastructure,
    override val credentials: ApiCredentials,
    override val cache: Cache
) : ApiProvider {
    private val myMergeRequestApi = GithubMergeRequestApi(infrastructure, credentials)

    override val info: ProviderInfo = Github

    override val user: UserApi = GithubUserApi(infrastructure, credentials)

    override val mergeRequest: MergeRequestApi = myMergeRequestApi

    override val project: ProjectApi = GithubProjectApi(infrastructure, credentials)

    override val comment: CommentApi
        get() = TODO("not implemented")

    override val commit: CommitApi
        get() = TODO("Not yet implemented")

    override fun setOptions(options: ApiOptions) {
    }
}