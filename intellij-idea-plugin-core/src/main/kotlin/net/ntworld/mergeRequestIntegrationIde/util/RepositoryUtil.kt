package net.ntworld.mergeRequestIntegrationIde.util

import com.intellij.dvcs.repo.VcsRepositoryManager
import com.intellij.openapi.project.Project as IdeaProject

object RepositoryUtil {
    fun getRepositoriesByProject(ideaProject: IdeaProject): List<String> {
        val vcsRepositoryManager = VcsRepositoryManager.getInstance(ideaProject)
        return vcsRepositoryManager.repositories.map {
            it.root.path
        }
    }
}