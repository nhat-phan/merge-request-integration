package net.ntworld.mergeRequestIntegrationIde.configuration.vos

import com.intellij.util.VersionUtil
import com.intellij.util.text.VersionComparatorUtil
import org.junit.Test
import kotlin.test.assertEquals

class GitRemotePathInfoTest {
    private data class Item(
            val input: String,
            val valid: Boolean,
            val namespace: String = "",
            val project: String = "",
            val toStringValue: String = ""
    )

    @Test
    fun testSshUrls() {
        val dataset = listOf(
                Item(
                        input = "git@gitlab.test.de:namespace/project.git",
                        valid = true,
                        namespace = "namespace",
                        project = "project",
                        toStringValue = "namespace/project"
                ),
                Item(
                        input = "git@gitlab.test.de:namespace/project",
                        valid = true,
                        namespace = "namespace",
                        project = "project",
                        toStringValue = "namespace/project"
                ),
                Item(
                        input = "git@gitlab.test.de:namespace",
                        valid = false
                ),
                Item(
                        input = "git@gitlab.test.de:",
                        valid = false
                )
        )

        dataset.forEach {
            val info = GitRemotePathInfo(it.input)

            assertEquals(it.valid, info.isValid, "failed case $it")
            if (it.valid) {
                assertEquals(it.namespace, info.namespace, "failed case $it")
                assertEquals(it.project, info.project, "failed case $it")
                assertEquals(it.toStringValue, info.toString(), "failed case $it")
            }
        }
    }

    @Test
    fun testHttpUrls() {
        val dataset = listOf(
                Item(
                        input = "https://gitlab.test.de/namespace/project",
                        valid = true,
                        namespace = "namespace",
                        project = "project",
                        toStringValue = "namespace/project"
                ),
                Item(
                        input = "https://gitlab.test.de/namespace/project.git",
                        valid = true,
                        namespace = "namespace",
                        project = "project",
                        toStringValue = "namespace/project"
                ),
                Item(
                        input = "http://gitlab.test.de/namespace/project.git",
                        valid = true,
                        namespace = "namespace",
                        project = "project",
                        toStringValue = "namespace/project"
                ),
                Item(
                        input = "HTTPS://gitlab.test.de/namespace/project",
                        valid = true,
                        namespace = "namespace",
                        project = "project",
                        toStringValue = "namespace/project"
                ),
                Item(
                        input = "http://gitlab.test.de/namespace/PROJECT.git",
                        valid = true,
                        namespace = "namespace",
                        project = "PROJECT",
                        toStringValue = "namespace/PROJECT"
                ),
                Item(
                        input = "https://github.com/nhat-phan/merge-request-integration",
                        valid = true,
                        namespace = "nhat-phan",
                        project = "merge-request-integration",
                        toStringValue = "nhat-phan/merge-request-integration"
                ),
                Item(
                        input = "http://github.com/nhat-phan/merge-request-integration",
                        valid = true,
                        namespace = "nhat-phan",
                        project = "merge-request-integration",
                        toStringValue = "nhat-phan/merge-request-integration"
                ),
                Item(
                        input = "http://github.com/nhat-phan",
                        valid = false
                ),
                Item(
                        input = "http:github.com/nhat-phan/merge-request-integration",
                        valid = false
                ),
                Item(
                        input = "http://:github.com:123\\:123nhat-phan/merge-request-integration",
                        valid = false
                )
        )

        dataset.forEach {
            val info: GitRemotePathInfo
            try{
                info = GitRemotePathInfo(it.input)
                assertEquals(it.valid, info.isValid, "failed case $it")
                if (it.valid) {
                    assertEquals(it.namespace, info.namespace, "failed case $it")
                    assertEquals(it.project, info.project, "failed case $it")
                    assertEquals(it.toStringValue, info.toString(), "failed case $it")
                }
            } catch (exception: Exception) {
                assertEquals(it.valid, false, "failed case $it")
            }
        }
    }

    @Test
    fun testVersion() {
        println(VersionComparatorUtil.compare("2020.2.0-eap-1-for-ide-2020.1.x", "2020.2.0-built-for-ide-2020.1.x"))
        println(VersionComparatorUtil.compare("2020.2.0-built-for-ide-2020.1.x", "2020.2.1-built-for-ide-2020.1.x"))
        println(VersionComparatorUtil.compare("2020.2.0-built-for-ide-2020.1.x", "2020.1.5-built-for-ide-2020.1"))
        println(VersionComparatorUtil.compare("2020.2.0-eap-1-for-ide-2020.1.x", "2020.1.5-built-for-ide-2020.1"))
    }
}