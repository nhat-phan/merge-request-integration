package net.ntworld.mergeRequestIntegration.provider.github.transformer

import net.ntworld.mergeRequest.User
import net.ntworld.mergeRequest.UserStatus
import net.ntworld.mergeRequestIntegration.DEFAULT_DATETIME
import net.ntworld.mergeRequestIntegration.internal.UserImpl
import net.ntworld.mergeRequestIntegration.provider.Transformer
import net.ntworld.mergeRequestIntegration.provider.github.vo.GithubUserId
import net.ntworld.mergeRequestIntegration.util.DateTimeUtil
import org.kohsuke.github.GHUser

object GithubUserTransformer : Transformer<GHUser, User> {
    override fun transform(input: GHUser): User = UserImpl(
        // Unlike gitlab, github works based on login rather than id
        // So to keep everything works as expected, we have to add username into id information
        // Please use GithubUserId value-object to work with generate/parsing user id
        id = GithubUserId(input.id, input.login).getValue(),
        name = input.name,
        username = input.login,
        avatarUrl = input.avatarUrl,
        url = input.htmlUrl.toString(),
        status = UserStatus.ACTIVE,
        email = input.email,
        createdAt = if (null === input.createdAt) DEFAULT_DATETIME else DateTimeUtil.fromDate(input.createdAt)
    )
}