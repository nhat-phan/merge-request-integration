package net.ntworld.mergeRequestIntegrationIde.ui.mergeRequest.tab.commit

import net.ntworld.mergeRequest.Commit

object CommitSelectUtil {
    fun canSelect(list: List<Pair<Commit, Boolean>>, index: Int): Boolean {
        if (isAllSelectedOrUnselected(list)) {
            return true
        }

        val range = findSelectedCommitRangeIndex(list)
        return range.first - 1 <= index && index <= range.second + 1
    }

    private fun findSelectedCommitRangeIndex(list: List<Pair<Commit, Boolean>>): Pair<Int, Int> {
        var first = -1
        var last = -1
        var setFirst = false
        var index = 0
        for (item in list) {
            if (item.second) {
                if (!setFirst) {
                    first = index
                    setFirst = true
                }
                last = index
                index++
                continue
            }

            if (setFirst) {
                break
            }
            index++
        }
        return Pair(first, last)
    }

    private fun isAllSelectedOrUnselected(list: List<Pair<Commit, Boolean>>): Boolean {
        if (list.isEmpty() || list.size == 1) {
            return true
        }

        val first = list[0].second
        for (i in 1..list.lastIndex) {
            if (list[i].second != first) {
                return false
            }
        }
        return true
    }
}