package net.ntworld.mergeRequestIntegrationIde.comments

import com.intellij.codeInsight.daemon.OutsidersPsiFileSupport
import com.intellij.diff.tools.simple.SimpleOnesideDiffViewer
import com.intellij.dvcs.repo.VcsRepositoryManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.actionSystem.EditorAction
import com.intellij.openapi.editor.actionSystem.EditorActionHandler
import com.intellij.openapi.editor.colors.EditorColors
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.editor.impl.EditorEmbeddedComponentManager
import com.intellij.openapi.editor.impl.EditorImpl
import com.intellij.openapi.editor.impl.view.FontLayoutService
import com.intellij.openapi.editor.markup.HighlighterLayer
import com.intellij.openapi.fileTypes.FileTypes
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vcs.changes.Change
import com.intellij.openapi.vcs.changes.actions.diff.ChangeDiffRequestProducer
import com.intellij.openapi.wm.IdeFocusManager
import com.intellij.ui.EditorTextField
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.panels.Wrapper
import com.intellij.util.ui.GraphicsUtil
import com.intellij.util.ui.JBInsets
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import com.intellij.util.ui.components.BorderLayoutPanel
import git4idea.repo.GitRepository
import net.ntworld.mergeRequest.Comment
import net.ntworld.mergeRequestIntegrationIde.comments.ui.CommentsThreadComponent
import net.ntworld.mergeRequestIntegrationIde.service.ApplicationService
import net.ntworld.mergeRequestIntegrationIde.service.ProjectService
import net.ntworld.mergeRequestIntegrationIde.ui.util.RepositoryUtil
import java.awt.*
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.event.InputEvent
import java.awt.event.KeyEvent
import java.awt.geom.RoundRectangle2D
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.ScrollPaneConstants
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

class OneSideViewerCommentsController(
        private val applicationService: ApplicationService,
        private val viewer: SimpleOnesideDiffViewer,
        private val change: Change
) {
    private val projectService = applicationService.getProjectService(viewer.project!!)
    private val commentsList: MutableList<Comment> = mutableListOf()

    private val editor: EditorImpl = viewer.editor as EditorImpl

    private val changeType: Change.Type = change.type

    init {
        val project = viewer.project!!

        val virtualFile = viewer.editor.virtualFile

        val vcsRepositoryManager = VcsRepositoryManager.getInstance(project)

        val repository = vcsRepositoryManager.repositories.first() as GitRepository

        projectService.codeReviewManager?.comments
                ?.filter { null !== it.position }
                ?.forEach { comment: Comment ->
                    if (change.type == Change.Type.NEW
                            && RepositoryUtil.findAbsolutePath(repository, comment.position!!.newPath!!)
                            == OutsidersPsiFileSupport.getOriginalFilePath(viewer.editor.virtualFile)) {

                        commentsList.add(comment)
                    }
                }

        installLineMarkers()
        installCreateCommentHandlerShortcut()
    }

    private fun installCreateCommentHandlerShortcut() {
        CreateCommentActionHandlerForOneSideViewer(viewer, commentsList)
                .registerCustomShortcutSet(KeyEvent.VK_V, InputEvent.SHIFT_MASK, viewer.editor.component)
    }

    private fun installLineMarkers() {
        val markupModel = viewer.editor.markupModel

        commentsList.forEach { comment: Comment ->
            val line = comment.position!!.newLine!!
            markupModel.addLineHighlighter(line, HighlighterLayer.LAST, null)
                    .gutterIconRenderer = GutterCommentLineMarkerRenderer(line, object : DumbAwareAction() {
                override fun actionPerformed(e: AnActionEvent) {
                    projectService.notify("Click on comment ICON", NotificationType.INFORMATION)

                    commentsList.takeIf { it.size > 0 }?.let {
                        val comment = it[0]

                        //prepare comments thread component
//                        val roundedPanel = RoundedPanel()
                        val commentsThread = JBUI.Panels.simplePanel(CommentsThreadComponent(comment))
                                .withBorder(JBUI.Borders.empty(12, 12))
                                .andTransparent()

//                        roundedPanel.setContent(commentsThread)
                        //finish

                        val offset = editor.document.getLineEndOffset(comment.position!!.newLine!!)

                        EditorEmbeddedComponentManager.getInstance()
                                .addComponent(editor as EditorEx, commentsThread,
                                        EditorEmbeddedComponentManager.Properties(EditorEmbeddedComponentManager.ResizePolicy.any(),
                                                true, false, 0, offset))
                    }
                }
            })
        }
    }

    class CreateCommentActionHandlerForOneSideViewer(
            val viewer: SimpleOnesideDiffViewer,
            val comments: MutableList<Comment>
    ) : EditorAction(object : EditorActionHandler() {

        private lateinit var editor: EditorImpl

        override fun doExecute(editor: Editor, caret: Caret?, dataContext: DataContext?) {
            super.doExecute(editor, caret, dataContext)

            this.editor = editor as EditorImpl

            val change = viewer.request.getUserData(ChangeDiffRequestProducer.CHANGE_KEY)!!

            //--------------- field for insert comment
//            val commentField = CommentFieldUIComponentBuilder().build(editor.project!!).apply {
//                border = JBUI.Borders.empty(12)
//            }
//
//            val roundedPanel = RoundedPanel()
//            roundedPanel.setContent(commentField)
//
//            roundedPanel.isEnabled = true

//            ------------ insert after
//            val wrappedComponent = ComponentWrapper(roundedPanel, EditorTextWidthWatcher(editor))
//            val offset = editor.document.getLineEndOffset(editor.caretModel.logicalPosition.line)
//
//            EditorEmbeddedComponentManager.getInstance()
//                    .addComponent(editor as EditorEx, wrappedComponent,
//                            EditorEmbeddedComponentManager.Properties(EditorEmbeddedComponentManager.ResizePolicy.any(),
//                                    true, false, 0, offset))
//------------------------------
//            val focusManager = IdeFocusManager.findInstanceByComponent(commentField)
//            val toFocus = focusManager.getFocusTargetFor(commentField) ?: return
//            focusManager.doWhenFocusSettlesDown { focusManager.requestFocus(toFocus, true) }

// ---------------------
//            val container = BorderLayoutPanel().andTransparent()
//            val button = JButton("actionName" + StringUtil.ELLIPSIS).apply {
//                isOpaque = false
//                putClientProperty(UIUtil.HIDE_EDITOR_FROM_DATA_CONTEXT_PROPERTY, true)
//            }
//            val buttonWrapper = Wrapper(button).apply {
//                border = JBUI.Borders.emptyLeft(28)
//            }
        }

        @Deprecated(message = "use factory instead")
        private inner class RoundedPanel : Wrapper() {
            private var borderLineColor: Color? = null

            init {
                cursor = Cursor.getDefaultCursor()
                updateColors()
            }

            override fun updateUI() {
                super.updateUI()
                updateColors()
            }

            private fun updateColors() {
                val scheme = EditorColorsManager.getInstance().globalScheme
                background = scheme.defaultBackground
                borderLineColor = scheme.getColor(EditorColors.TEARLINE_COLOR)
            }

            override fun paintComponent(g: Graphics) {
                GraphicsUtil.setupRoundedBorderAntialiasing(g)

                val g2 = g as Graphics2D
                val rect = Rectangle(size)
                JBInsets.removeFrom(rect, insets)
                // 2.25 scale is a @#$!% so we adjust sizes manually
                val rectangle2d = RoundRectangle2D.Float(rect.x.toFloat() + 0.5f, rect.y.toFloat() + 0.5f,
                        rect.width.toFloat() - 1f, rect.height.toFloat() - 1f,
                        10f, 10f)
                g2.color = background
                g2.fill(rectangle2d)
                borderLineColor?.let {
                    g2.color = borderLineColor
                    g2.draw(rectangle2d)
                }
            }
        }

        private inner class ComponentWrapper(
                private val component: JComponent,
                private val editorWidthWatcher: EditorTextWidthWatcher
        ) : JBScrollPane(component) {
            init {
                isOpaque = false
                viewport.isOpaque = false

                border = JBUI.Borders.empty()
                viewportBorder = JBUI.Borders.empty()

                horizontalScrollBarPolicy = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
                verticalScrollBar.preferredSize = Dimension(0, 0)
                setViewportView(component)

                component.addComponentListener(object : ComponentAdapter() {
                    override fun componentResized(e: ComponentEvent) = dispatchEvent(ComponentEvent(component, ComponentEvent.COMPONENT_RESIZED))
                })
            }

            override fun getPreferredSize(): Dimension {
                return Dimension(editorWidthWatcher.editorTextWidth, component.preferredSize.height)
            }
        }

        private inner class EditorTextWidthWatcher(private val editor: EditorImpl) : ComponentAdapter() {

            var editorTextWidth: Int = 0

            private val maximumEditorTextWidth: Int
            private val verticalScrollbarFlipped: Boolean

            init {
                val metrics = editor.getFontMetrics(Font.PLAIN)
                val spaceWidth = FontLayoutService.getInstance().charWidth2D(metrics, ' '.toInt())
                // -4 to create some space
                maximumEditorTextWidth = ceil(spaceWidth * (editor.settings.getRightMargin(editor.project)) - 4).toInt()

                val scrollbarFlip = editor.scrollPane.getClientProperty(JBScrollPane.Flip::class.java)
                verticalScrollbarFlipped = scrollbarFlip == JBScrollPane.Flip.HORIZONTAL || scrollbarFlip == JBScrollPane.Flip.BOTH
            }

            override fun componentResized(e: ComponentEvent) = updateWidthForAllInlays()
            override fun componentHidden(e: ComponentEvent) = updateWidthForAllInlays()
            override fun componentShown(e: ComponentEvent) = updateWidthForAllInlays()

            private fun updateWidthForAllInlays() {
                val newWidth = calcWidth()
                if (editorTextWidth == newWidth) return
                editorTextWidth = newWidth

//            managedInlays.keys.forEach {
//                it.dispatchEvent(ComponentEvent(it, ComponentEvent.COMPONENT_RESIZED))
//                it.invalidate()
//            }
            }

            private fun calcWidth(): Int {
                val visibleEditorTextWidth = editor.scrollPane.viewport.width - editor.scrollPane.verticalScrollBar.width - if (verticalScrollbarFlipped) 4 else 0
                return min(max(visibleEditorTextWidth, 0), maximumEditorTextWidth)
            }
        }
    })
}