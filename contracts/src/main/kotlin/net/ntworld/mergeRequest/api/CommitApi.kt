package net.ntworld.mergeRequest.api

import net.ntworld.mergeRequest.Change

interface CommitApi {

    fun getChanges(projectId: String, commitId: String): List<Change>

}