package net.ntworld.mergeRequestIntegrationIde.internal

import org.junit.Test
import kotlin.test.assertEquals

class CommentNodeDataImplTest {

    @Test
    fun `test getHash`() {
        data class Item(val general: Boolean, val fileName: String, val line: Int, val output: String)
        val dataset = listOf(
            Item(false, "CodeReviewService.kt", 5, "CodeReviewService.kt-0000000005-app/impl/CodeReviewService.kt"),
            Item(false, "CodeReviewService.kt", 20, "CodeReviewService.kt-0000000020-app/impl/CodeReviewService.kt"),
            Item(false, "CommentNodeData.kt", 165, "CommentNodeData.kt-0000000165-app/impl/CommentNodeData.kt"),
            Item(false, "CommentNodeData.kt", 2020, "CommentNodeData.kt-0000002020-app/impl/CommentNodeData.kt"),
            Item(true, "CommentNodeData.kt", 2020, "__________GENERAL")
        )
        for (item in dataset) {
            assertEquals(
                item.output,
                CommentNodeDataImpl(
                    isGeneral = item.general,
                    fullPath = "app/impl/${item.fileName}",
                    fileName = item.fileName,
                    line = item.line
                ).getHash()
            )
        }

    }
}