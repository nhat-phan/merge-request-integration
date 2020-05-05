package net.ntworld.mergeRequestIntegrationIde

import com.intellij.openapi.diagnostic.Logger

const val ENTERPRISE_EDITION_URL = "https://plugins.jetbrains.com/plugin/13615-merge-request-integration-ee--code-review-for-gitlab/"
const val SEARCH_MERGE_REQUEST_ITEMS_PER_PAGE = 10

const val SINGLE_MR_CHANGES_WHEN_DOING_CODE_REVIEW_NAME = "Changed Files"
const val SINGLE_MR_REWORK_CHANGES_PREFIX = "Files"
const val SINGLE_MR_REWORK_COMMENTS_PREFIX = "Comments"

private const val DEBUG = true

val logger = Logger.getInstance("MRI")
fun debug(message: String) {
    if (DEBUG) {
        println(message)
        logger.debug(message)
    }
}
