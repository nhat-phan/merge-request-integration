package net.ntworld.mergeRequestIntegrationIde.infrastructure.service

import com.intellij.openapi.vcs.changes.Change

interface RepositoryFileService {

    fun loadChange(path: String, commits: List<String>): Change

}