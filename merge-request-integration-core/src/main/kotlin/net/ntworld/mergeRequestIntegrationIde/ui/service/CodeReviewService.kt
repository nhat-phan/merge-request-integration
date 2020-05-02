package net.ntworld.mergeRequestIntegrationIde.ui.service

import com.intellij.notification.NotificationType
import com.intellij.openapi.wm.ToolWindowManager
import net.ntworld.mergeRequest.Commit
import net.ntworld.mergeRequest.MergeRequest
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequestIntegrationIde.infrastructure.ApplicationServiceProvider
import net.ntworld.mergeRequestIntegrationIde.infrastructure.ProjectServiceProvider
import net.ntworld.mergeRequestIntegrationIde.infrastructure.notifier.ProjectNotifier
import kotlin.Exception
import com.intellij.openapi.project.Project as IdeaProject

object CodeReviewService {
    var checkedOut = false

    fun start(
        projectServiceProvider: ProjectServiceProvider,
        providerData: ProviderData,
        mergeRequest: MergeRequest,
        commits: List<Commit>
    ) {
        checkedOut = false
        projectServiceProvider.reviewContextManager.updateReviewingCommits(providerData.id, mergeRequest.id, commits)
        projectServiceProvider.startCodeReview(providerData, mergeRequest)
        checkout(projectServiceProvider, providerData, mergeRequest, commits)
    }

    fun stop(
        projectServiceProvider: ProjectServiceProvider,
        providerData: ProviderData,
        mergeRequest: MergeRequest
    ) {
        projectServiceProvider.stopCodeReview()
        if (checkedOut) {
            CheckoutService.stop(projectServiceProvider, providerData)
            DisplayChangesService.stop(projectServiceProvider.project, providerData, mergeRequest)
        }
    }

    private fun checkout(
        projectServiceProvider: ProjectServiceProvider,
        providerData: ProviderData,
        mergeRequest: MergeRequest,
        commits: List<Commit>
    ) {
        if (!projectServiceProvider.applicationSettings.checkoutTargetBranch) {
            return checkoutSuccess(projectServiceProvider, providerData, mergeRequest, commits)
        }

        CheckoutService.start(projectServiceProvider, providerData, mergeRequest, object : CheckoutService.Listener {
            override fun onError(exception: Exception) {
                projectServiceProvider.notify(
                    "Cannot checkout branch ${mergeRequest.sourceBranch}\n\nPlease do git checkout manually before click Code Review",
                    NotificationType.ERROR
                )
                this@CodeReviewService.stop(projectServiceProvider, providerData, mergeRequest)
            }

            override fun onSuccess() {
                checkoutSuccess(projectServiceProvider, providerData, mergeRequest, commits)
            }
        })
    }

    private fun checkoutSuccess(
        projectServiceProvider: ProjectServiceProvider,
        providerData: ProviderData,
        mergeRequest: MergeRequest,
        commits: List<Commit>
    ) {
        checkedOut = true
        EditorStateService.start(projectServiceProvider.project)
        DisplayChangesService.start(
            projectServiceProvider.applicationServiceProvider,
            projectServiceProvider.project,
            providerData, mergeRequest, commits
        )
    }
}