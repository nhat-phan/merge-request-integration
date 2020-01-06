package net.ntworld.mergeRequest.api

import net.ntworld.mergeRequest.Project
import net.ntworld.mergeRequest.UserInfo

interface ProjectApi {
    fun find(projectId: String): Project?

    fun getMembers(projectId: String): List<UserInfo>

    fun findOrFail(projectId: String): Project {
        val project = find(projectId)
        if (null === project) {
            throw Exception("Project $projectId not found.")
        }
        return project
    }
}