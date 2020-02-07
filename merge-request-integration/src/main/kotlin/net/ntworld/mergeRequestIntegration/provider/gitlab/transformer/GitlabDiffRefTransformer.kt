package net.ntworld.mergeRequestIntegration.provider.gitlab.transformer

import net.ntworld.mergeRequest.DiffReference
import net.ntworld.mergeRequestIntegration.internal.DiffReferenceImpl
import net.ntworld.mergeRequestIntegration.provider.Transformer
import org.gitlab4j.api.models.DiffRef

object GitlabDiffRefTransformer:
    Transformer<DiffRef, DiffReference> {
    override fun transform(input: DiffRef): DiffReference = DiffReferenceImpl(
        baseHash = input.baseSha,
        headHash = input.headSha,
        startHash = input.startSha
    )
}