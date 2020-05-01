package net.ntworld.mergeRequestIntegrationIde.ui.service

import com.intellij.openapi.components.ServiceManager
import git4idea.branch.GitBrancher
import com.intellij.openapi.project.Project as IdeaProject
import git4idea.repo.GitRepository
import net.ntworld.mergeRequest.MergeRequest
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequestIntegrationIde.infrastructure.ProjectServiceProvider
import net.ntworld.mergeRequestIntegrationIde.util.RepositoryUtil
import kotlin.Exception

object CheckoutService {
    private var myCurrentBranch: String? = null

    fun stop(projectServiceProvider: ProjectServiceProvider, providerData: ProviderData) {
        val branch = myCurrentBranch
        val repository = RepositoryUtil.findRepository(projectServiceProvider, providerData)
        if (null !== branch && null !== repository) {
            doCheckout(projectServiceProvider, false, repository, providerData, branch, object : Listener {
                override fun onError(exception: Exception) {
                    myCurrentBranch = null
                }

                override fun onSuccess() {
                    myCurrentBranch = null
                }
            })
        }
    }

    fun start(projectServiceProvider: ProjectServiceProvider, providerData: ProviderData, mergeRequest: MergeRequest, listener: Listener) {
        val repository = RepositoryUtil.findRepository(projectServiceProvider, providerData)
        if (null === repository) {
            return listener.onError(Exception("Repository not found"))
        }
        doCheckout(projectServiceProvider, true, repository, providerData, mergeRequest.sourceBranch, listener)
    }

    private fun doCheckout(
        projectServiceProvider: ProjectServiceProvider,
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

        val branchExecutor = ServiceManager.getService(projectServiceProvider.project, GitBrancher::class.java)
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