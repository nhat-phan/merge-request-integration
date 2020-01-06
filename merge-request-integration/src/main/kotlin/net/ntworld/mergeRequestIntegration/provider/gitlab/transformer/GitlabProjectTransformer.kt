package net.ntworld.mergeRequestIntegration.provider.gitlab.transformer

import net.ntworld.mergeRequest.Project
import net.ntworld.mergeRequest.ProjectVisibility
import net.ntworld.mergeRequestIntegration.internal.ProjectImpl
import net.ntworld.mergeRequestIntegration.provider.gitlab.Gitlab
import net.ntworld.mergeRequestIntegration.provider.gitlab.Transformer
import org.gitlab4j.api.models.Visibility
import org.gitlab4j.api.models.Project as GitlabProject

object GitlabProjectTransformer : Transformer<GitlabProject, Project> {

    override fun transform(input: GitlabProject): Project = ProjectImpl(
        id = input.id.toString(),
        provider = Gitlab,
        name = input.name,
        path = input.path,
        url = input.webUrl,
        visibility = if (input.visibility == Visibility.PUBLIC) ProjectVisibility.PUBLIC else ProjectVisibility.PRIVATE,
        avatarUrl = input.avatarUrl ?: "",
        repositoryHttpUrl = input.httpUrlToRepo,
        repositorySshUrl = input.sshUrlToRepo
    )

}