package net.ntworld.mergeRequestIntegration.provider.gitlab.transformer

import net.ntworld.mergeRequest.Change
import net.ntworld.mergeRequestIntegration.internal.ChangeImpl
import net.ntworld.mergeRequestIntegration.provider.Transformer
import org.gitlab4j.api.models.Diff

object GitlabDiffTransformer : Transformer<Diff, Change> {
    override fun transform(input: Diff): Change = ChangeImpl(
        oldPath = input.oldPath,
        newPath = input.newPath,
        aMode = input.aMode,
        bMode = input.bMode,
        newFile = input.newFile,
        renamedFile = input.renamedFile,
        deletedFile = input.deletedFile
    )
}