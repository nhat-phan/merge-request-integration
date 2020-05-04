package net.ntworld.mergeRequestIntegrationIde.rework

import com.intellij.openapi.fileEditor.TextEditor

interface EditorManager {
    fun initialize(textEditor: TextEditor, reworkWatcher: ReworkWatcher)

    fun updateComments(textEditor: TextEditor, reworkWatcher: ReworkWatcher)

    fun shutdown(textEditor: TextEditor)
}