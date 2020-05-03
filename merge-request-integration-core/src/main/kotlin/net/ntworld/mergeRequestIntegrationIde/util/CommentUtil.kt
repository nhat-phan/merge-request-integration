package net.ntworld.mergeRequestIntegrationIde.util

import net.ntworld.mergeRequest.Comment

object CommentUtil {

    fun groupCommentsByThreadId(comments: List<Comment>): Map<String, List<Comment>> {
        val groups = mutableMapOf<String, MutableList<Comment>>()
        for (comment in comments) {
            if (!groups.containsKey(comment.parentId)) {
                groups[comment.parentId] = mutableListOf()
            }
            groups[comment.parentId]!!.add(comment)
        }
        return groups
    }

    fun groupCommentsByNewPath(comments: List<Comment>): Map<String, List<Comment>> {
        val groups = mutableMapOf<String, MutableList<Comment>>()
        for (comment in comments) {
            val position = comment.position
            if (null === position) {
                continue
            }
            val path = position.newPath
            if (null === path) {
                continue
            }

            if (!groups.containsKey(path)) {
                groups[path] = mutableListOf()
            }
            groups[path]!!.add(comment)
        }
        return groups
    }

    fun groupCommentsByPositionPath(comments: List<Comment>): Map<String, List<Comment>> {
        val groups = mutableMapOf<String, MutableList<Comment>>()
        for (comment in comments) {
            val position = comment.position
            if (null === position) {
                continue
            }
            val path = if (null !== position.newPath) position.newPath else position.oldPath
            if (null === path) {
                continue
            }

            if (!groups.containsKey(path)) {
                groups[path] = mutableListOf()
            }
            groups[path]!!.add(comment)
        }
        return groups
    }

    fun groupCommentsByPositionLine(comments: List<Comment>): Map<Int, List<Comment>> {
        val groups = mutableMapOf<Int, MutableList<Comment>>()
        for (comment in comments) {
            val position = comment.position
            if (null === position) {
                continue
            }
            val line = if (null !== position.newLine) position.newLine else position.oldLine
            if (null === line) {
                continue
            }

            if (!groups.containsKey(line)) {
                groups[line] = mutableListOf()
            }
            groups[line]!!.add(comment)
        }
        return groups.toSortedMap()
    }
}