package net.ntworld.mergeRequestIntegrationIde.ui.service

import com.intellij.openapi.components.ServiceManager
import git4idea.branch.GitBrancher
import com.intellij.openapi.project.Project as IdeaProject
import git4idea.repo.GitRepository
import net.ntworld.mergeRequest.MergeRequest
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequestIntegrationIde.ui.util.RepositoryUtil
import kotlin.Exception

object CheckoutService {
    private var myCurrentBranch: String? = null

    fun stop(ideaProject: IdeaProject, providerData: ProviderData, mergeRequest: MergeRequest) {
        val branch = myCurrentBranch
        val repository = RepositoryUtil.findRepository(ideaProject, providerData)
        if (null !== branch && null !== repository) {
            doCheckout(ideaProject, false, repository, providerData, branch, object : Listener {
                override fun onError(exception: Exception) {
                    myCurrentBranch = null
                }

                override fun onSuccess() {
                    myCurrentBranch = null
                }
            })
        }
    }

    fun start(ideaProject: IdeaProject, providerData: ProviderData, mergeRequest: MergeRequest, listener: Listener) {
        val repository = RepositoryUtil.findRepository(ideaProject, providerData)
        if (null === repository) {
            return listener.onError(Exception("Repository not found"))
        }
        doCheckout(ideaProject, true, repository, providerData, mergeRequest.sourceBranch, listener)
    }

    private fun doCheckout(
        ideaProject: IdeaProject,
        useRemote: Boolean,
        repository: GitRepository,
        providerData: ProviderData,
        branch: String,
        listener: Listener
    ) {
        val currentBranch = repository.currentBranch
        if (null !== currentBranch) {
            if (currentBranch.name == branch) {
                return listener.onSuccess()
            }
            myCurrentBranch = currentBranch.name
        }

        val branchExecutor = ServiceManager.getService(ideaProject, GitBrancher::class.java)
        try {
            branchExecutor.checkout(branch, false, listOf(repository)) { listener.onSuccess() }
        } catch (exception: Exception) {
            if (!useRemote) {
                return listener.onError(exception)
            }

            val remoteName = findRemoteName(repository, providerData)
            if (remoteName.isEmpty()) {
                return listener.onError(Exception("Cannot find remote of repository"))
            }

            try {
                branchExecutor.checkoutNewBranchStartingFrom(
                    branch, "$remoteName/${branch}",
                    false, listOf(repository)
                ) {
                    listener.onSuccess()
                }
            } catch (exception: Exception) {
                listener.onError(exception)
            }
        }
    }

    private fun findRemoteName(repository: GitRepository, providerData: ProviderData): String {
        for (remote in repository.remotes) {
            for (url in remote.urls) {
                if (url == providerData.project.repositoryHttpUrl || url == providerData.project.repositorySshUrl) {
                    return remote.name
                }
            }
        }
        return ""
    }

    interface Listener {
        fun onError(exception: Exception)

        fun onSuccess()
    }
}