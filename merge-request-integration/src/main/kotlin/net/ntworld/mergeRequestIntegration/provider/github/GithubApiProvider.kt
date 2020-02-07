package net.ntworld.mergeRequestIntegration.provider.github

import net.ntworld.foundation.Infrastructure
import net.ntworld.mergeRequest.ProviderInfo
import net.ntworld.mergeRequest.api.*

class GithubApiProvider(
    override val credentials: ApiCredentials,
    private val infrastructure: Infrastructure,
    override val cache: Cache
) : ApiProvider {
    override val info: ProviderInfo = Github

    override val user: UserApi
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    override val mergeRequest: MergeRequestApi
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    override val project: ProjectApi
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    override val comment: CommentApi
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    override fun setOptions(options: ApiOptions) {
    }
}