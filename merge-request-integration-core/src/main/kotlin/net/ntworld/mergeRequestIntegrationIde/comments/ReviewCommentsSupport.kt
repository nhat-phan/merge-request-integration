package net.ntworld.mergeRequestIntegrationIde.comments

import com.intellij.codeInsight.daemon.OutsidersPsiFileSupport
import com.intellij.diff.requests.ContentDiffRequest
import com.intellij.diff.tools.fragmented.UnifiedDiffViewer
import com.intellij.diff.tools.fragmented.UnifiedFragmentBuilder
import com.intellij.diff.tools.simple.SimpleOnesideDiffViewer
import com.intellij.diff.tools.util.base.DiffViewerBase
import com.intellij.diff.tools.util.side.TwosideTextDiffViewer
import com.intellij.diff.util.DiffUtil
import com.intellij.diff.util.Side
import com.intellij.notification.NotificationType
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorAction
import com.intellij.openapi.editor.actionSystem.EditorActionHandler
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.editor.impl.event.MarkupModelListener
import com.intellij.openapi.progress.util.ProgressIndicatorBase
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.changes.Change
import com.intellij.openapi.vcs.changes.actions.diff.ChangeDiffRequestProducer.CHANGE_KEY
import net.ntworld.mergeRequestIntegrationIde.service.ProjectService
import java.awt.event.InputEvent.SHIFT_MASK
import java.awt.event.KeyEvent.VK_V

@Service
class ReviewCommentsSupport(private val project: Project) {

    fun installExtensions(viewer: DiffViewerBase, request: ContentDiffRequest) {
        when (viewer) {
            is SimpleOnesideDiffViewer -> {
                println("Simple oneside viewer")
                OneSideViewerCommentsController(viewer, viewer.request.getUserData(CHANGE_KEY)!!)
            }
            is TwosideTextDiffViewer -> {
                println("Side by side viewer")
                val editor = viewer.getEditor(Side.RIGHT)

                CreateCommentAtRightAction(viewer)
                        .registerCustomShortcutSet(VK_V, SHIFT_MASK, editor.component)
            }
            is UnifiedDiffViewer -> println("Unified viewer")
            else -> println("Unsupported viewer")
        }


    }

    class CreateCommentAtRightAction(
            val viewer: TwosideTextDiffViewer
    ) : EditorAction(object : EditorActionHandler() {

        override fun doExecute(editor: Editor, caret: Caret?, dataContext: DataContext?) {
            super.doExecute(editor, caret, dataContext)

            val change = viewer.request.getUserData(CHANGE_KEY)!!
            val diffProvider = DiffUtil
                    .createTextDiffProvider(viewer.project, viewer.request, viewer.textSettings, {}, {})

            val list = diffProvider.compare(viewer.content1.document.immutableCharSequence,
                    viewer.content2.document.immutableCharSequence, ProgressIndicatorBase())

//            val patchReader = PatchVirtualFileReader.create((editor as EditorEx).virtualFile)
//            patchReader.parseAllPatches()

            //мы находимся в контексте двустороннего вьювера, поэтому у нас возможны следующие случаи
            //1. комментарий на строке в которой не было изменений, и в таком случае нам нужно вычислить две стороны oldLine -> newLine
            //2. комментарий на строке с новым кодом oldLine:null -> newLine
            //3. комментарий на строке с удаленным кодом oldline -> newLine:null
            //4. комментарий на строке которая не переехала, но при этом текст изменился

            //calculate position
            //call comment UI presenter
            //another stuff

            //I'm not sure is it good to create a lot of handlers, one for each editor
            //but with current approach we can calculate position more accurate


            val originalFilePath = OutsidersPsiFileSupport.getOriginalFilePath((editor as EditorEx).virtualFile)
            val position = DiffUtil.getCaretPosition(editor)


//            val lineNumber = viewer.currentSide.other()
//                    .select(viewer.getContent(Side.LEFT).document, viewer.getContent(Side.RIGHT).document)
//                    ?.getLineNumber(editor.caretModel.offset)


            val fragmentBuilder = UnifiedFragmentBuilder(list!!, viewer.content1.document, viewer.content2.document, Side.RIGHT)
            fragmentBuilder.exec()


            val lineNumber = viewer.currentSide.other().select(fragmentBuilder.convertor1, fragmentBuilder.convertor2)
                    ?.convert(position.line) //converter works with index position

            ProjectService.getInstance(editor.project!!)
                    .notify("caret position: ${position.line + 1}\n " +
                            "line number: ${lineNumber?.plus(1) ?: "null"}\n" +
                            "file path: $originalFilePath",
                            NotificationType.INFORMATION)

            val codeReviewManager = ProjectService.getInstance(editor.project!!)
                    .codeReviewManager!!


            val gitRepository = codeReviewManager.repository
            val mergeRequest = codeReviewManager.mergeRequest

            val changeInfo = codeReviewManager
                    .findChangeInfoByPathAndContent(originalFilePath!!, editor.document.text)

            ProjectService.getInstance(editor.project!!)
                    .notify(changeInfo.toString(),
                            NotificationType.INFORMATION)

        }
    })

    class ChangesCalculator {

        fun create() {

        }
    }

    interface LineMappingUtil {
        fun findCommentLine(change: Change)
        fun findFile()
    }

    companion object {
        @JvmStatic
        fun getInstance(project: Project): ReviewCommentsSupport =
                ServiceManager.getService(project, ReviewCommentsSupport::class.java)
    }
}