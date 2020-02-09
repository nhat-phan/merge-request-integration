package net.ntworld.mergeRequestIntegration.provider.github

import net.ntworld.foundation.Infrastructure
import net.ntworld.mergeRequest.ProviderInfo
import net.ntworld.mergeRequest.api.*

class GithubApiProvider(
    private val infrastructure: Infrastructure,
    override val credentials: ApiCredentials,
    override val cache: Cache
) : ApiProvider {
    override val info: ProviderInfo = Github

    override val user: UserApi = GithubUserApi(infrastructure, credentials)

    override val mergeRequest: MergeRequestApi
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    override val project: ProjectApi = GithubProjectApi(infrastructure, credentials)

    override val comment: CommentApi
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    override fun setOptions(options: ApiOptions) {
    }
}