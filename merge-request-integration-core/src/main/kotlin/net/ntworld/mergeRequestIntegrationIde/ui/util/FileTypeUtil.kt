package net.ntworld.mergeRequestIntegrationIde.ui.util

import com.intellij.lang.Language
import com.intellij.openapi.fileTypes.LanguageFileType
import javax.swing.Icon

object FileTypeUtil {
    private class MarkdownLanguage : Language("Markdown", "text/x-markdown")
    private class MarkdownFileType(language: Language): LanguageFileType(language) {
        override fun getIcon(): Icon? = null

        override fun getName(): String {
            return "Markdown"
        }

        override fun getDefaultExtension(): String {
            return "md"
        }

        override fun getDescription(): String {
            return "Markdown"
        }
    }

    val markdownFileType : LanguageFileType by lazy {
        MarkdownFileType(getLanguage())
    }

    private fun getLanguage() : Language {
        val definedLanguages = Language.findInstancesByMimeType("text/x-markdown")
        if (definedLanguages.isEmpty()) {
            return MarkdownLanguage()
        }
        return definedLanguages.first()
    }
}