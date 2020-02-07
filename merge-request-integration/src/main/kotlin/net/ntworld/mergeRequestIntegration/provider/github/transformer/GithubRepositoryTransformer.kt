package net.ntworld.mergeRequestIntegration.provider.github.transformer

import net.ntworld.mergeRequest.Project
import net.ntworld.mergeRequest.ProjectVisibility
import net.ntworld.mergeRequestIntegration.internal.ProjectImpl
import net.ntworld.mergeRequestIntegration.provider.github.Github
import net.ntworld.mergeRequestIntegration.provider.gitlab.Transformer
import org.kohsuke.github.GHRepository

object GithubRepositoryTransformer : Transformer<GHRepository, Project> {
    override fun transform(input: GHRepository): Project = ProjectImpl(
        id = input.id.toString(),
        provider = Github,
        name = input.name,
        path = input.fullName,
        url = input.url.toString(),
        visibility = if (input.isPrivate) ProjectVisibility.PRIVATE else ProjectVisibility.PUBLIC,
        avatarUrl = "",
        repositoryHttpUrl = input.htmlUrl.toString(),
        repositorySshUrl = input.sshUrl
    )
}