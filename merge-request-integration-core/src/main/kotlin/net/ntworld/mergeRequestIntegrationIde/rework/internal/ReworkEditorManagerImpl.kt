package net.ntworld.mergeRequestIntegrationIde.rework.internal

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx
import com.intellij.openapi.vfs.LocalFileSystem
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequestIntegrationIde.infrastructure.ProjectServiceProvider
import net.ntworld.mergeRequestIntegrationIde.rework.ReworkEditorController
import net.ntworld.mergeRequestIntegrationIde.rework.ReworkEditorManager
import net.ntworld.mergeRequestIntegrationIde.rework.ReworkWatcher

class ReworkEditorManagerImpl(
    private val projectServiceProvider: ProjectServiceProvider
) : ReworkEditorManager {
    private val myFileEditorManagerEx = FileEditorManagerEx.getInstanceEx(projectServiceProvider.project)
    private val myControllerMap = mutableMapOf<TextEditor, ReworkEditorController>()

    override fun bootstrap(reworkWatcher: ReworkWatcher) {
        val editors = FileEditorManagerEx.getInstance(projectServiceProvider.project).allEditors
        for (editor in editors) {
            if (editor is TextEditor) {
                bootstrap(editor, reworkWatcher)
            }
        }
    }

    override fun bootstrap(editor: TextEditor, reworkWatcher: ReworkWatcher) {
        bindControllerToTextEditor(editor, reworkWatcher)
    }

    override fun open(providerData: ProviderData, path: String, line: Int?) {
        val file = LocalFileSystem.getInstance().findFileByPath(path) ?: return

        ApplicationManager.getApplication().invokeLater {
            val editors = myFileEditorManagerEx.openFile(file, true)
            for (fileEditor in editors) {
                if (fileEditor is TextEditor) {
                    val controller = bindControllerToTextEditor(fileEditor, providerData)
                    if (null !== line && null !== controller) {
                        scrollAndDisplayCommentsOnLine(controller, line)
                    }
                }
            }
        }
    }

    private fun scrollAndDisplayCommentsOnLine(controller: ReworkEditorController, line: Int) {
        controller.hideAllComments()
        controller.scrollToLine(line)
        controller.displayCommentsOnLine(line)
    }

    override fun commentsUpdated(providerData: ProviderData) {
        ApplicationManager.getApplication().invokeLater {
            val editors = myFileEditorManagerEx.allEditors
            for (editor in editors) {
                if (editor is TextEditor) {
                    val controller = myControllerMap[editor] ?: continue
                    controller.updateComments()
                }
            }
        }
    }

    override fun shutdown(providerData: ProviderData) {
        val editors = myFileEditorManagerEx.allEditors
        for (editor in editors) {
            if (editor is TextEditor) {
                val controller = myControllerMap[editor] ?: continue
                controller.dispose()
            }
        }
    }

    private fun bindControllerToTextEditor(
        textEditor: TextEditor,
        providerData: ProviderData
    ): ReworkEditorController? {
        if (myControllerMap.contains(textEditor)) {
            return myControllerMap[textEditor]
        }

        val reworkWatcher = projectServiceProvider.reworkManager.findActiveReworkWatcher(providerData)
        if (null === reworkWatcher) {
            return null
        }
        return bindControllerToTextEditor(textEditor, reworkWatcher)
    }

    private fun bindControllerToTextEditor(
        textEditor: TextEditor,
        reworkWatcher: ReworkWatcher
    ): ReworkEditorController? {
        if (myControllerMap.contains(textEditor)) {
            return myControllerMap[textEditor]
        }

        val editor = textEditor.editor as? EditorEx ?: return null
        val virtualFile = textEditor.file ?: return null
        val change = reworkWatcher.findChangeByPath(virtualFile.path) ?: return null
        val afterRevision = change.afterRevision ?: return null

        val instance = ReworkEditorControllerImpl(
            projectServiceProvider,
            textEditor,
            editor,
            reworkWatcher.providerData,
            reworkWatcher.mergeRequestInfo,
            virtualFile.path,
            afterRevision.revisionNumber.toString()
        )
        myControllerMap[textEditor] = instance
        return instance
    }
}