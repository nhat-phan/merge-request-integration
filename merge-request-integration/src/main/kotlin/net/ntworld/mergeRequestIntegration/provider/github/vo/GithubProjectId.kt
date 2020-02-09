package net.ntworld.mergeRequestIntegration.provider.github.vo

data class GithubProjectId(
    val id: Long,
    val owner: String,
    val repo: String
) {
    fun getValue() : String {
        return "$id:$owner/$repo"
    }

    companion object {
        fun parse(input: String): GithubProjectId {
            val parts = input.split(":")
            if (parts.size != 2) {
                throw Exception("Invalid Github projectId")
            }
            val info = parts[1].split("/")
            if (info.size != 2) {
                throw Exception("Invalid Github projectId")
            }
            return GithubProjectId(
                id = parts[0].toLong(), owner = info[0], repo = info[1]
            )
        }

        fun parseId(projectId: String) : Long {
            return parse(projectId).id
        }

        fun parseFullName(projectId: String): String {
            val data = parse(projectId)
            return "${data.owner}/${data.repo}"
        }

        fun parseOwner(projectId: String) : String {
            return parse(projectId).owner
        }

        fun parseRepo(projectId: String): String {
            return parse(projectId).repo
        }
    }
}