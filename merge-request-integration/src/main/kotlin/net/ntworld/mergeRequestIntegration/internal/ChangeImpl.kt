package net.ntworld.mergeRequestIntegration.internal

import net.ntworld.mergeRequest.Change

data class ChangeImpl(
    override val oldPath: String,
    override val newPath: String,
    override val aMode: String,
    override val bMode: String,
    override val newFile: Boolean,
    override val renamedFile: Boolean,
    override val deletedFile: Boolean
) : Change