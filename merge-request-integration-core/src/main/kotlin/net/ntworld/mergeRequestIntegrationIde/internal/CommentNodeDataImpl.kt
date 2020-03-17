package net.ntworld.mergeRequestIntegrationIde.internal

import net.ntworld.mergeRequestIntegrationIde.GENERAL_NAME
import net.ntworld.mergeRequestIntegrationIde.service.CodeReviewUtil

data class CommentNodeDataImpl(
    override val isGeneral: Boolean,
    override val fullPath: String,
    override val fileName: String,
    override val line: Int
) : CodeReviewUtil.CommentNodeData {
    override fun getHash(): String {
        if (isGeneral) {
            return GENERAL_NAME
        }

        var lineText = line.toString()
        while (lineText.length < 10) {
            lineText = "0$lineText"
        }
        return "$fileName-$lineText-$fullPath"
    }
}
