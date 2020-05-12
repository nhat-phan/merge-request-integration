package net.ntworld.mergeRequestIntegrationIde.configuration.vos

import java.net.URL

class GitRemotePathInfo(private val input: String) {
    var namespace: String = ""
        private set

    var project: String = ""
        private set

    var isValid: Boolean = false
        private set

    init {
        val inputLowerCase = input.toLowerCase()
        if (inputLowerCase.startsWith("https://") || inputLowerCase.startsWith("http://")) {
            parseHttpUrl(input)
        }
        if (inputLowerCase.startsWith("git@")) {
            parseSshUrl(input)
        }
    }

    private fun parseSshUrl(input: String) {
        val index = input.lastIndexOf(':')
        if (index < 0) {
            return
        }
        val parts = input.substring(index + 1).split('/')
        if (parts.size != 2) {
            return
        }
        namespace = parts[0]
        project = if (parts[1].endsWith(".git")) parts[1].substring(0, parts[1].length - 4) else parts[1]
        isValid = true
    }

    private fun parseHttpUrl(input: String) {
        val url = URL(input)
        val parts = url.path.split('/')
        if (parts.size != 3) {
            return
        }
        namespace = parts[1]
        project = if (parts[2].endsWith(".git")) parts[2].substring(0, parts[2].length - 4) else parts[2]
        isValid = true
    }

    override fun toString(): String {
        return "$namespace/$project"
    }
}