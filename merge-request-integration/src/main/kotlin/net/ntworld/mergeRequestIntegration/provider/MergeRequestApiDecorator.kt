package net.ntworld.mergeRequestIntegration.provider

import net.ntworld.mergeRequest.api.MergeRequestApi

open class MergeRequestApiDecorator(
    private val wrappee: MergeRequestApi
): MergeRequestApi by wrappee
