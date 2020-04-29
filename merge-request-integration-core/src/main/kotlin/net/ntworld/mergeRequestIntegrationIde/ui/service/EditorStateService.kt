package net.ntworld.mergeRequestIntegrationIde.ui.service

import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx
import com.intellij.openapi.project.Project

object EditorStateService {
    fun start(ideaProject: Project) {
        val fileManager = FileEditorManagerEx.getInstance(ideaProject)
        val openFiles = fileManager.openFiles
        for (openFile in openFiles) {
            fileManager.closeFile(openFile)
        }
    }
}