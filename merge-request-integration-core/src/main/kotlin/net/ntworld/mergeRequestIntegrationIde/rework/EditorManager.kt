package net.ntworld.mergeRequestIntegrationIde.rework

import com.intellij.openapi.fileEditor.TextEditor

interface EditorManager {
    fun initialize(textEditor: TextEditor, watcher: ReworkWatcher)

    fun shutdown(textEditor: TextEditor)
}