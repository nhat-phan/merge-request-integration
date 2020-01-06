package net.ntworld.mergeRequestIntegrationIde.ui.editor

import com.intellij.codeInsight.daemon.OutsidersPsiFileSupport
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.openapi.vcs.changes.Change
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.project.Project as IdeaProject
import com.intellij.psi.PsiFileFactory

object EditorUtil {
    fun findVirtualFilePath(editor: EditorEx): String? {
        val virtualFile = editor.virtualFile

        if (null !== virtualFile) {
            val path = OutsidersPsiFileSupport.getOriginalFilePath(virtualFile)
            if (null !== path) {
                return path
            }
        }
        return null
    }

    fun openMergeRequestFile(ideaProject: IdeaProject) {
        val fileType = FileTypeManager.getInstance().findFileTypeByName("kt")
        if (null === fileType) {
            return
        }

        val psiFile = PsiFileFactory.getInstance(ideaProject).createFileFromText(
            "Test.kt",
            fileType,
            "class Test {}"
        )
        FileEditorManager.getInstance(ideaProject).openFile(psiFile.virtualFile, true)
    }
}