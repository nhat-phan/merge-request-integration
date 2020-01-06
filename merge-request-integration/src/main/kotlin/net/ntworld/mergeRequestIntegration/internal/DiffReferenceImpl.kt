package net.ntworld.mergeRequestIntegration.internal

import net.ntworld.mergeRequest.DiffReference

data class DiffReferenceImpl(
    override val baseHash: String,
    override val headHash: String,
    override val startHash: String
): DiffReference