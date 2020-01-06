package net.ntworld.mergeRequestIntegration.provider.gitlab.transformer

import net.ntworld.mergeRequest.UserInfo
import net.ntworld.mergeRequestIntegration.internal.UserInfoImpl
import net.ntworld.mergeRequestIntegration.provider.gitlab.GitlabUtil
import net.ntworld.mergeRequestIntegration.provider.gitlab.Transformer
import org.gitlab4j.api.models.AbstractUser
import org.gitlab4j.api.models.Member

object GitlabMemberTransformer: Transformer<AbstractUser<*>, UserInfo> {
    override fun transform(input: AbstractUser<*>): UserInfo = UserInfoImpl(
        id = input.getId().toString(),
        name = input.getName(),
        username = input.getUsername(),
        avatarUrl = input.getAvatarUrl(),
        url = input.getWebUrl(),
        status = GitlabUtil.findUserStatus(input.getState())
    )
}