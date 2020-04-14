package net.ntworld.mergeRequestIntegrationIde.infrastructure.service.repositoryFile

import com.intellij.openapi.vcs.changes.Change
import net.ntworld.mergeRequestIntegrationIde.infrastructure.service.RepositoryFileService

class LocalRepositoryFileService : RepositoryFileService {

    override fun loadChange(path: String, commits: List<String>): Change {
        TODO()
    }

}