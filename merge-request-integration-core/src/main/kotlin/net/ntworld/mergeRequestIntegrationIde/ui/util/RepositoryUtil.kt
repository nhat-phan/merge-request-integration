package net.ntworld.mergeRequestIntegrationIde.ui.util

import com.intellij.dvcs.repo.VcsRepositoryManager
import com.intellij.openapi.project.Project as IdeaProject
import git4idea.repo.GitRepository
import net.ntworld.mergeRequest.ProviderData
import java.io.File

object RepositoryUtil {
    private val repositoryCached = mutableMapOf<String, GitRepository>()

    fun findRepository(ideaProject: IdeaProject, providerData: ProviderData): GitRepository? {
        if (null === repositoryCached[providerData.id]) {
            val vcsRepositoryManager = VcsRepositoryManager.getInstance(ideaProject)
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
}