package net.ntworld.mergeRequestIntegration.provider.github.transformer

import net.ntworld.mergeRequest.Project
import net.ntworld.mergeRequest.ProjectVisibility
import net.ntworld.mergeRequestIntegration.internal.ProjectImpl
import net.ntworld.mergeRequestIntegration.provider.github.Github
import net.ntworld.mergeRequestIntegration.provider.Transformer
import net.ntworld.mergeRequestIntegration.provider.github.vo.GithubProjectId
import org.kohsuke.github.GHRepository

object GithubRepositoryTransformer :
    Transformer<GHRepository, Project> {
    override fun transform(input: GHRepository): Project = ProjectImpl(
        // Unlike gitlab, github works based on :owner/:repo rather than id
        // So to keep everything works as expected, we have to add :owner/:repo information
        // Please use GithubProjectId value-object to work with generate/parsing user id
        id = GithubProjectId(id = input.id, owner = input.ownerName, repo = input.name).getValue(),
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