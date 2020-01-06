package net.ntworld.mergeRequestIntegrationIde.ui.service

import net.ntworld.mergeRequest.Commit
import net.ntworld.mergeRequest.MergeRequest
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequestIntegrationIde.service.ProjectService
import kotlin.Exception
import com.intellij.openapi.project.Project as IdeaProject

object CodeReviewService {
    var checkedOut = false

    fun start(ideaProject: IdeaProject, providerData: ProviderData, mergeRequest: MergeRequest, commits: List<Commit>) {
        checkedOut = false
        val projectService = ProjectService.getInstance(ideaProject)
        projectService.setCodeReviewCommits(providerData, mergeRequest, commits)
        projectService.dispatcher.multicaster.startCodeReview(providerData, mergeRequest)
        checkout(ideaProject, providerData, mergeRequest, commits)
    }

    fun stop(ideaProject: IdeaProject, providerData: ProviderData, mergeRequest: MergeRequest) {
        ProjectService.getInstance(ideaProject).dispatcher.multicaster.stopCodeReview(providerData, mergeRequest)
        if (checkedOut) {
            CheckoutService.stop(ideaProject, providerData, mergeRequest)
            EditorStateService.stop(ideaProject, providerData, mergeRequest)
            DisplayChangesService.stop(ideaProject, providerData, mergeRequest)
        }
    }

    private fun checkout(
        ideaProject: IdeaProject,
        providerData: ProviderData,
        mergeRequest: MergeRequest,
        commits: List<Commit>
    ) {
        CheckoutService.start(ideaProject, providerData, mergeRequest, object : CheckoutService.Listener {
            override fun onError(exception: Exception) {
                this@CodeReviewService.stop(ideaProject, providerData, mergeRequest)
            }

            override fun onSuccess() {
                checkedOut = true
                EditorStateService.start(ideaProject, providerData, mergeRequest)
                DisplayChangesService.start(ideaProject, providerData, mergeRequest, commits)
            }
        })
    }
}