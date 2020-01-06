package net.ntworld.mergeRequestIntegrationIde.ui.util

import com.intellij.dvcs.repo.VcsRepositoryManager
import com.intellij.openapi.project.Project
import git4idea.repo.GitRepository
import net.ntworld.mergeRequest.ProviderData
import java.io.File

object RepositoryUtil {
    fun findRepository(ideaProject: Project, providerData: ProviderData): GitRepository? {
        val vcsRepositoryManager = VcsRepositoryManager.getInstance(ideaProject)
        for (repository in vcsRepositoryManager.repositories) {
            if (repository.root.path == providerData.repository) {
                return repository as GitRepository
            }
        }
        return null
    }

    fun findAbsolutePath(repository: GitRepository?, relativePath: String): String {
        if (null === repository) {
            return relativePath.replace('/', File.separatorChar)
        }
        return "${repository.root.path}${File.separatorChar}${relativePath.replace('/', File.separatorChar)}"
    }

    fun findRelativePath(repository: GitRepository?, absolutePath: String): String {
        if (null === repository) {
            return absolutePath.replace(File.separatorChar, '/')
        }
        val root = "${repository.root.path}${File.separatorChar}"
        if (!absolutePath.startsWith(root)) {
            return absolutePath.replace(File.separatorChar, '/')
        }
        return absolutePath.substring(root.length).replace(File.separatorChar, '/')
    }
}