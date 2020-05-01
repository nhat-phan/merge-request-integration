package net.ntworld.mergeRequestIntegrationIde

import com.intellij.openapi.diagnostic.Logger

const val ENTERPRISE_EDITION_URL = "https://plugins.jetbrains.com/plugin/13615-merge-request-integration-ee--code-review-for-gitlab/"
const val SEARCH_MERGE_REQUEST_ITEMS_PER_PAGE = 10

private const val DEBUG = true

val logger = Logger.getInstance("MRI")
fun debug(message: String) {
    if (DEBUG) {
        println(message)
        logger.debug(message)
    }
}
