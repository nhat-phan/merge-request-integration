package net.ntworld.mergeRequestIntegrationIde.compatibility

import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.editor.impl.EditorEmbeddedComponentManager
import com.intellij.openapi.project.Project
import com.intellij.vcs.log.impl.VcsLogManager
import gnu.trove.TIntFunction

interface IntellijIdeApi {
    fun findLeftLineNumberConverter(editor: EditorEx): TIntFunction

    fun findRightLineNumberConverter(editor: EditorEx): TIntFunction

    fun makeEditorEmbeddedComponentManagerProperties(offset: Int): EditorEmbeddedComponentManager.Properties

    fun getVcsLogManager(ideaProject: Project, action: ((VcsLogManager) -> Unit))
}