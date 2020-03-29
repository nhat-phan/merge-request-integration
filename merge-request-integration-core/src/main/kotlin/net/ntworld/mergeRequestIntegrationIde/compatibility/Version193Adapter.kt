//package net.ntworld.mergeRequestIntegrationIde.compatibility
//
//import com.intellij.openapi.editor.ex.EditorEx
//import com.intellij.openapi.editor.impl.EditorEmbeddedComponentManager
//import gnu.trove.TIntFunction
//
//class Version193Adapter : IntellijIdeApi {
//    private val resizePolicy by lazy {
//        val constructors = EditorEmbeddedComponentManager.ResizePolicy::class.java.declaredConstructors
//        for (ctor in constructors) {
//            ctor.isAccessible = true
//            return@lazy ctor.newInstance(0) as EditorEmbeddedComponentManager.ResizePolicy
//        }
//        return@lazy EditorEmbeddedComponentManager.ResizePolicy.any()
//    }
//
//    override fun findLeftLineNumberConverter(editor: EditorEx): TIntFunction {
//        val myLineNumberConvertor = editor.gutter.javaClass.getDeclaredField("myLineNumberConvertor")
//        myLineNumberConvertor.isAccessible = true
//        return myLineNumberConvertor.get(editor.gutter) as TIntFunction
//    }
//
//    override fun findRightLineNumberConverter(editor: EditorEx): TIntFunction {
//        val myAdditionalLineNumberConvertor = editor.gutter.javaClass.getDeclaredField(
//            "myAdditionalLineNumberConvertor"
//        )
//        myAdditionalLineNumberConvertor.isAccessible = true
//        return myAdditionalLineNumberConvertor.get(editor.gutter) as TIntFunction
//    }
//
//    override fun makeEditorEmbeddedComponentManagerProperties(offset: Int): EditorEmbeddedComponentManager.Properties {
//        return EditorEmbeddedComponentManager.Properties(
//            resizePolicy,
//            true,
//            false,
//            0,
//            offset
//        )
//    }
//}