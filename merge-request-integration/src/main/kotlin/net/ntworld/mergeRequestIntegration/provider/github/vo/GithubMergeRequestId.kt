package net.ntworld.mergeRequestIntegration.provider.github.vo

data class GithubMergeRequestId(
    val id: Long,
    val number: Int,
    val nodeId: String
) {
    fun getValue() : String {
        return "$id:$number:$nodeId"
    }

    companion object {
        fun parse(input: String): GithubMergeRequestId {
            val parts = input.split(":")
            if (parts.size != 3) {
                throw Exception("Invalid Github mergeRequestId")
            }
            return GithubMergeRequestId(
                id = parts[0].toLong(),
                number = parts[1].toInt(),
                nodeId = parts[2]
            )
        }

        fun parseId(mergeRequestId: String) : Long {
            return parse(mergeRequestId).id
        }

        fun parseNumber(mergeRequestId: String) : Int {
            return parse(mergeRequestId).number
        }

        fun parseNodeId(mergeRequestId: String) : String {
            return parse(mergeRequestId).nodeId
        }
    }
}