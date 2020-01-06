package net.ntworld.mergeRequest

interface Project {
    val id: String

    val provider: ProviderInfo

    val name: String

    val path: String

    val url: String

    val avatarUrl: String

    val visibility: ProjectVisibility

    val repositoryHttpUrl: String

    val repositorySshUrl: String

    companion object
}
