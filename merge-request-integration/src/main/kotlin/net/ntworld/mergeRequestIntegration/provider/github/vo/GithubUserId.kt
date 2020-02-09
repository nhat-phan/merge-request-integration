package net.ntworld.mergeRequestIntegration.provider.github.vo

data class GithubUserId(
    val id: Long,
    val login: String
) {
    fun getValue() : String {
        return "$id:$login"
    }

    companion object {
        fun parse(input: String): GithubUserId {
            val parts = input.split(":")
            if (parts.size != 2) {
                throw Exception("Invalid Github userId")
            }
            return GithubUserId(
                id = parts[0].toLong(), login = parts[1]
            )
        }

        fun parseId(input: String): Long {
            return parse(input).id
        }

        fun parseLogin(input: String): String {
            return parse(input).login
        }
    }
}