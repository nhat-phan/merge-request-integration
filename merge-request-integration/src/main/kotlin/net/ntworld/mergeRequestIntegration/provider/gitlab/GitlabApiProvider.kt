package net.ntworld.mergeRequestIntegration.provider.gitlab

import net.ntworld.foundation.Infrastructure
import net.ntworld.mergeRequest.ProviderInfo
import net.ntworld.mergeRequest.UserInfo
import net.ntworld.mergeRequest.api.*
import net.ntworld.mergeRequestIntegration.provider.DraftCommentApi
import net.ntworld.mergeRequestIntegration.provider.MemoryDraftCommentStorage

class GitlabApiProvider(
    private val infrastructure: Infrastructure,

    override val credentials: ApiCredentials,

    override val cache: Cache
) : ApiProvider {
    private var myCommentApi: CommentApi? = null
    private val myMergeRequestApi = GitlabMergeRequestApiCache(
        GitlabMergeRequestApi(infrastructure, credentials), cache
    )

    override val info: ProviderInfo = Gitlab

    override val user: UserApi = GitlabUserApi(infrastructure, credentials)

    override val mergeRequest: MergeRequestApi = myMergeRequestApi

    override val project: ProjectApi = GitlabProjectApi(infrastructure, credentials)

    override val comment: CommentApi
        get() = myCommentApi!!

    override val commit: CommitApi = GitlabCommitApi(infrastructure, credentials)

    override fun setOptions(options: ApiOptions) {
        myMergeRequestApi.options = options
    }

    override fun initialize(currentUser: UserInfo) {
        this.myCommentApi = DraftCommentApi(
            GitlabCommentApi(infrastructure, credentials),
            MemoryDraftCommentStorage(currentUser)
        )
    }
}