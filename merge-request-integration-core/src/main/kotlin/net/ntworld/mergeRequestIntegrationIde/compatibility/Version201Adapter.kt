package net.ntworld.mergeRequestIntegrationIde.compatibility

import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.editor.impl.EditorEmbeddedComponentManager
import com.intellij.openapi.editor.impl.LineNumberConverterAdapter
import gnu.trove.TIntFunction

class Version201Adapter : IntellijIdeApi {
    override fun findLeftLineNumberConverter(editor: EditorEx): TIntFunction {
        val myLineNumberConverter = editor.gutter.javaClass.getDeclaredField("myLineNumberConverter")
        myLineNumberConverter.isAccessible = true
        val adapter = myLineNumberConverter.get(editor.gutter) as LineNumberConverterAdapter
        return TIntFunction { p0 -> adapter.convert(editor, p0) ?: -1 }
    }

    override fun findRightLineNumberConverter(editor: EditorEx): TIntFunction {
        val myAdditionalLineNumberConverter = editor.gutter.javaClass.getDeclaredField(
            "myAdditionalLineNumberConverter"
        )
        myAdditionalLineNumberConverter.isAccessible = true
        val adapter = myAdditionalLineNumberConverter.get(editor.gutter) as LineNumberConverterAdapter
        return TIntFunction { p0 -> adapter.convert(editor, p0) ?: -1 }
    }

    override fun makeEditorEmbeddedComponentManagerProperties(offset: Int): EditorEmbeddedComponentManager.Properties {
        return EditorEmbeddedComponentManager.Properties(
            EditorEmbeddedComponentManager.ResizePolicy.none(),
            null,
            true,
            false,
            0,
            offset
        )
    }
}