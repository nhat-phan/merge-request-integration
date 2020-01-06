package net.ntworld.mergeRequestIntegration.provider.gitlab

import net.ntworld.foundation.Infrastructure
import net.ntworld.mergeRequest.ProviderInfo
import net.ntworld.mergeRequest.api.*

class GitlabApiProvider(
    override val credentials: ApiCredentials,

    private val infrastructure: Infrastructure
) : ApiProvider {

    override val info: ProviderInfo = Gitlab

    override val user: UserApi = GitlabUserApi(infrastructure, credentials)

    override val mergeRequest: MergeRequestApi = GitlabMergeRequestApi(infrastructure, credentials)

    override val project: ProjectApi = GitlabProjectApi(infrastructure, credentials)

    override val comment: CommentApi = GitlabCommentApi(infrastructure, credentials)

}