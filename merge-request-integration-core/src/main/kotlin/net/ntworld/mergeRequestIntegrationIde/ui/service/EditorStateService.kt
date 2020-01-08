package net.ntworld.mergeRequestIntegrationIde.ui.service

import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx
import com.intellij.openapi.project.Project
import net.ntworld.mergeRequest.MergeRequest
import net.ntworld.mergeRequest.ProviderData

object EditorStateService {
    fun stop(ideaProject: Project, providerData: ProviderData, mergeRequest: MergeRequest) {

    }

    fun start(ideaProject: Project, providerData: ProviderData, mergeRequest: MergeRequest) {
        val fileManager = FileEditorManagerEx.getInstance(ideaProject)
        val openFiles = fileManager.openFiles
        for (openFile in openFiles) {
            fileManager.closeFile(openFile)
        }
    }
}