package net.ntworld.mergeRequestIntegration.provider.gitlab.transformer

import net.ntworld.mergeRequest.UserInfo
import net.ntworld.mergeRequestIntegration.internal.UserInfoImpl
import net.ntworld.mergeRequestIntegration.provider.gitlab.GitlabUtil
import net.ntworld.mergeRequestIntegration.provider.Transformer
import org.gitlab4j.api.models.User as UserModel

object GitlabUserInfoTransformer:
    Transformer<UserModel, UserInfo> {
    override fun transform(input: UserModel): UserInfo = UserInfoImpl(
        id = input.id.toString(),
        name = input.name,
        username = input.username,
        avatarUrl = input.avatarUrl,
        url = input.webUrl,
        status = GitlabUtil.findUserStatus(input.state)
    )


}