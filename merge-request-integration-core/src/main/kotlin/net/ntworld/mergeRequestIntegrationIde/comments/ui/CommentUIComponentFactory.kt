package net.ntworld.mergeRequestIntegrationIde.comments.ui

import com.intellij.openapi.editor.colors.EditorColors
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.ui.components.panels.Wrapper
import com.intellij.util.ui.GraphicsUtil
import com.intellij.util.ui.JBInsets
import com.intellij.util.ui.JBUI
import net.ntworld.mergeRequest.Comment
import java.awt.*
import java.awt.geom.RoundRectangle2D
import javax.swing.JComponent

class CommentUIComponentFactory {

    fun createCommentThreadComponent(thread: Comment): JComponent? {
        val roundedPanel = RoundedPanel()

        val commentsThread = JBUI.Panels.simplePanel(CommentsThreadComponent(thread))
                .withBorder(JBUI.Borders.empty(12, 12))
                .andTransparent()

        roundedPanel.setContent(commentsThread)

        return roundedPanel
    }

    fun createCommentComponent(comment: Comment): JComponent? {
        return null
    }

    private class RoundedPanel : Wrapper() {
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
}