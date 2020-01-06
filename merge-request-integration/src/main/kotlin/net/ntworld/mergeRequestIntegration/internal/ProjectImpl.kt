package net.ntworld.mergeRequestIntegration.internal

import net.ntworld.mergeRequest.Project
import net.ntworld.mergeRequest.ProjectVisibility
import net.ntworld.mergeRequest.ProviderInfo

data class ProjectImpl(
    override val id: String,
    override val provider: ProviderInfo,
    override val name: String,
    override val path: String,
    override val url: String,
    override val avatarUrl: String,
    override val visibility: ProjectVisibility,
    override val repositoryHttpUrl: String,
    override val repositorySshUrl: String
): Project