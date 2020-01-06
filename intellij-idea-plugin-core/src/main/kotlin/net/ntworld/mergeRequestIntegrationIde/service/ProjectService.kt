package net.ntworld.mergeRequestIntegrationIde.service

import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.vcs.changes.Change
import com.intellij.util.EventDispatcher
import net.ntworld.mergeRequest.*
import com.intellij.openapi.project.Project as IdeaProject
import net.ntworld.mergeRequest.api.ApiCredentials

interface ProjectService {

    val dispatcher: EventDispatcher<ProjectEventListener>

    val commentStore: CommentStore

    val codeReviewManager: CodeReviewManager?

    val codeReviewUtil: CodeReviewUtil

    val registeredProviders: List<ProviderData>

    fun supported(): List<ProviderInfo>

    fun addProviderConfiguration(id: String, info: ProviderInfo, credentials: ApiCredentials, repository: String)

    fun removeProviderConfiguration(id: String)

    fun getProviderConfigurations(): List<ProviderSettings>

    fun clear()

    fun register(settings: ProviderSettings)

    fun isDoingCodeReview(): Boolean

    fun isReviewing(providerData: ProviderData, mergeRequest: MergeRequest): Boolean

    fun setCodeReviewComments(
        providerData: ProviderData,
        mergeRequest: MergeRequest,
        comments: Collection<Comment>
    )

    fun getCodeReviewComments(): Collection<Comment>

    fun setCodeReviewCommits(
        providerData: ProviderData,
        mergeRequest: MergeRequest,
        commits: Collection<Commit>
    )

    fun getCodeReviewCommits(): Collection<Commit>

    fun setCodeReviewChanges(
        providerData: ProviderData,
        mergeRequest: MergeRequest,
        changes: Collection<Change>
    )

    fun getCodeReviewChanges(): Collection<Change>

    companion object {
        fun getInstance(ideaProject: IdeaProject): ProjectService {
            return ServiceManager.getService(ideaProject, ProjectService::class.java)
        }
    }
}
