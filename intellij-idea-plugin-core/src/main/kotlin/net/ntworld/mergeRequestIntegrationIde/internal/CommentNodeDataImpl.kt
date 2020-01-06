package net.ntworld.mergeRequestIntegrationIde.internal

import net.ntworld.mergeRequestIntegrationIde.service.CodeReviewUtil

data class CommentNodeDataImpl(
    override val isGeneral: Boolean,
    override val fullPath: String,
    override val fileName: String,
    override val line: Int
) : CodeReviewUtil.CommentNodeData {
    override fun getHash(): String {
        if (isGeneral) {
            return "__________GENERAL"
        }

        var lineText = line.toString()
        while (lineText.length < 10) {
            lineText = "0$lineText"
        }
        return "$fileName-$lineText-$fullPath"
    }
}
