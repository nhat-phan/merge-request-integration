package net.ntworld.mergeRequestIntegrationIde.compatibility

import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.editor.impl.EditorEmbeddedComponentManager
import gnu.trove.TIntFunction

interface IntellijIdeApi {
    fun findLeftLineNumberConverter(editor: EditorEx): TIntFunction

    fun findRightLineNumberConverter(editor: EditorEx): TIntFunction

    fun makeEditorEmbeddedComponentManagerProperties(offset: Int): EditorEmbeddedComponentManager.Properties
}