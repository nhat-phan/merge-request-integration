package net.ntworld.mergeRequestIntegration.util

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import net.ntworld.mergeRequest.MergeRequestState
import net.ntworld.mergeRequest.api.MergeRequestOrdering
import net.ntworld.mergeRequest.query.GetMergeRequestFilter
import net.ntworld.mergeRequest.query.generated.GetMergeRequestFilterImpl

class SavedFiltersUtil {
    @Serializable
    private class SavedFilters constructor(
        val state: String,
        val search: String,
        val authorId: String,
        val assigneeId: String,
        val approverIds: List<String>,
        val ordering: String
    ) {
        fun toPair(): Pair<GetMergeRequestFilter, MergeRequestOrdering> {
            return Pair(
                GetMergeRequestFilterImpl(
                    id = null,
                    state = when (state) {
                        "all" -> MergeRequestState.ALL
                        "opened" -> MergeRequestState.OPENED
                        "closed" -> MergeRequestState.CLOSED
                        "merged" -> MergeRequestState.MERGED
                        else -> MergeRequestState.OPENED
                    },
                    search = search,
                    authorId = authorId,
                    assigneeId = assigneeId,
                    approverIds = approverIds,
                    sourceBranch = ""
                ),
                when (ordering) {
                    "recently-updated" -> MergeRequestOrdering.RECENTLY_UPDATED
                    "newest" -> MergeRequestOrdering.NEWEST
                    "oldest" -> MergeRequestOrdering.OLDEST
                    else -> MergeRequestOrdering.RECENTLY_UPDATED
                }
            )
        }
    }

    companion object {
        @JvmStatic
        private val json = Json(JsonConfiguration.Stable.copy(strictMode = false))

        @JvmStatic
        fun stringify(filter: GetMergeRequestFilter, ordering: MergeRequestOrdering): String {
            val data = SavedFilters(
                state = when (filter.state) {
                    MergeRequestState.ALL -> "all"
                    MergeRequestState.OPENED -> "opened"
                    MergeRequestState.CLOSED -> "closed"
                    MergeRequestState.MERGED -> "merged"
                },
                search = filter.search,
                authorId = filter.authorId,
                assigneeId = filter.assigneeId,
                approverIds = filter.approverIds,
                ordering = when (ordering) {
                    MergeRequestOrdering.RECENTLY_UPDATED -> "recently-updated"
                    MergeRequestOrdering.NEWEST -> "newest"
                    MergeRequestOrdering.OLDEST -> "oldest"
                }
            )
            return json.stringify(SavedFilters.serializer(), data)
        }

        @JvmStatic
        fun parse(input: String) : Pair<GetMergeRequestFilter, MergeRequestOrdering>? {
            return try {
                val data = json.parse(SavedFilters.serializer(), input)
                data.toPair()
            } catch (exception: Exception) {
                null
            }
        }
    }
}