package net.ntworld.mergeRequestIntegration.provider.gitlab.transformer

import net.ntworld.mergeRequest.User
import net.ntworld.mergeRequestIntegration.internal.UserImpl
import net.ntworld.mergeRequestIntegration.provider.gitlab.GitlabUtil
import net.ntworld.mergeRequestIntegration.provider.gitlab.Transformer
import org.gitlab4j.api.models.User as UserModel

object GitlabUserTransformer: Transformer<UserModel, User> {
    override fun transform(input: UserModel): User = UserImpl(
        id = input.id.toString(),
        name = input.name,
        username = input.username,
        avatarUrl = input.avatarUrl,
        url = input.webUrl,
        status = GitlabUtil.findUserStatus(input.state),
        email = input.email,
        createdAt = input.createdAt.toString()
    )
}