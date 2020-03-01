package net.ntworld.mergeRequestIntegrationIde.util

import com.intellij.dvcs.repo.VcsRepositoryManager
import git4idea.repo.GitRepository
import com.intellij.openapi.project.Project as IdeaProject

object RepositoryUtil {
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