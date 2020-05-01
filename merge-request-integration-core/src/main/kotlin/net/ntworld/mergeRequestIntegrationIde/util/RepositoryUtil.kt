package net.ntworld.mergeRequestIntegrationIde.util

import com.intellij.dvcs.repo.VcsRepositoryManager
import git4idea.repo.GitRepository
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequestIntegrationIde.infrastructure.ProjectServiceProvider
import java.io.File
import com.intellij.openapi.project.Project as IdeaProject

object RepositoryUtil {
    private val repositoryCached = mutableMapOf<String, GitRepository>()

    fun findRepository(projectServiceProvider: ProjectServiceProvider, providerData: ProviderData): GitRepository? {
        if (null === repositoryCached[providerData.id]) {
            val vcsRepositoryManager = VcsRepositoryManager.getInstance(projectServiceProvider.project)
            for (repository in vcsRepositoryManager.repositories) {
                if (repository.root.path == providerData.repository) {
                    repositoryCached[providerData.id] = repository as GitRepository
                    return repository
                }
            }
        }
        return repositoryCached[providerData.id]
    }

    fun transformToCrossPlatformsPath(input: String): String {
        return if (input.contains('\\')) input.replace('\\', '/') else input
    }

    fun findAbsoluteCrossPlatformsPath(repository: GitRepository?, relativePath: String): String {
        if (null === repository) {
            return transformToCrossPlatformsPath(relativePath)
        }
        return transformToCrossPlatformsPath("${repository.root.path}${File.separatorChar}$relativePath")
    }

    fun findRelativePath(repository: GitRepository?, absolutePath: String): String {
        if (null === repository) {
            return absolutePath.replace(File.separatorChar, '/')
        }
        val root = repository.root.path
        if (!absolutePath.startsWith(root)) {
            return absolutePath.replace(File.separatorChar, '/')
        }
        val path = absolutePath.substring(root.length).replace(File.separatorChar, '/')
        return if (path.startsWith('/')) {
            path.substring(1)
        } else {
            path
        }
    }

    fun getRepositoriesByProject(ideaProject: IdeaProject): List<String> {
        val vcsRepositoryManager = VcsRepositoryManager.getInstance(ideaProject)
        return vcsRepositoryManager.repositories.map {
            it.root.path
        }
    }

    fun findRepositoryByPath(ideaProject: IdeaProject, path: String): GitRepository? {
        val vcsRepositoryManager = VcsRepositoryManager.getInstance(ideaProject)
        val repository = vcsRepositoryManager.repositories.find {
            it.root.path == path
        }
        if (null === repository || repository !is GitRepository) {
            return null
        }
        return repository
    }
}