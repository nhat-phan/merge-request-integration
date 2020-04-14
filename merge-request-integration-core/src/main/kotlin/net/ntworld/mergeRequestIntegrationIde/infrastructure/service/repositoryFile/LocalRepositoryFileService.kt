package net.ntworld.mergeRequestIntegrationIde.infrastructure.service.repositoryFile

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.vcs.changes.Change
import com.intellij.vcs.log.impl.VcsLogContentUtil
import com.intellij.vcs.log.util.VcsLogUtil
import net.ntworld.mergeRequest.ProviderData
import com.intellij.openapi.project.Project as IdeaProject
import net.ntworld.mergeRequestIntegrationIde.infrastructure.service.RepositoryFileService
import net.ntworld.mergeRequestIntegrationIde.util.RepositoryUtil

class LocalRepositoryFileService(
    private val ideaProject: IdeaProject
) : RepositoryFileService {
    private val myLogger = Logger.getInstance(this.javaClass)

    override fun findChanges(providerData: ProviderData, hashes: List<String>): List<Change> {
        try {
            val repository = RepositoryUtil.findRepository(ideaProject, providerData)
            if (null === repository) {
                return listOf()
            }
            val log = VcsLogContentUtil.getOrCreateLog(ideaProject)
            if (null === log) {
                return listOf()
            }

            val details = VcsLogUtil.getDetails(
                log.dataManager.getLogProvider(repository.root),
                repository.root,
                hashes
            )
            return VcsLogUtil.collectChanges(details) { it.changes }
        } catch (exception: Exception) {
            myLogger.info("Cannot findChanges for ${providerData.repository}, hashes: ${hashes.joinToString(",")}")
            throw exception
        }
    }

}