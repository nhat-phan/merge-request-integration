package net.ntworld.mergeRequestIntegrationIde.diff.gutter

enum class GutterState {
    NO_COMMENT,
    THREAD_HAS_SINGLE_COMMENT,
    THREAD_HAS_MULTI_COMMENTS,
    WRITING
}