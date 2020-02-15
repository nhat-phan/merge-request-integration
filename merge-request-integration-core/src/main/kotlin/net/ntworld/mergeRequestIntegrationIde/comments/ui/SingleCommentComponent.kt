package net.ntworld.mergeRequestIntegrationIde.comments.ui

import com.intellij.icons.AllIcons
import com.intellij.ui.components.labels.LinkLabel
import com.intellij.util.ui.UI
import com.intellij.util.ui.UIUtil
import net.miginfocom.layout.CC
import net.miginfocom.layout.LC
import net.miginfocom.swing.MigLayout
import net.ntworld.mergeRequest.Comment
import javax.swing.JPanel

class SingleCommentComponent(comment: Comment) : JPanel() {

    private val avatarLabel: LinkLabel<*>
    private val titlePane: HtmlEditorPane
    private val textPane: HtmlEditorPane

    init {
        isOpaque = false
        layout = MigLayout(LC().gridGap("0", "0")
                .insets("0", "0", "0", "0")
                .fill()).apply {
            columnConstraints = "[]${UI.scale(8)}[]"
        }

        avatarLabel = LinkLabel.create("") {

            comment.author
//            comment.authorLinkUrl?.let { BrowserUtil.browse(it) }

        }.apply {
            //            icon = avatarIconsProvider.getIcon(comment.authorAvatarUrl)
            icon = AllIcons.General.ArrowUp
            isFocusable = true
            putClientProperty(UIUtil.HIDE_EDITOR_FROM_DATA_CONTEXT_PROPERTY, true)
        }

//        val href = comment.authorLinkUrl?.let { """href='${it}'""" }.orEmpty()
        val href = ""
        //language=HTML
        val title = """<a $href>${comment.author.username}</a> commented ${comment.createdAt}"""

        titlePane = HtmlEditorPane(title).apply {
            foreground = UIUtil.getContextHelpForeground()
            putClientProperty(UIUtil.HIDE_EDITOR_FROM_DATA_CONTEXT_PROPERTY, true)
        }

        textPane = HtmlEditorPane(comment.body).apply {
            putClientProperty(UIUtil.HIDE_EDITOR_FROM_DATA_CONTEXT_PROPERTY, true)
        }

        add(avatarLabel, CC().pushY())
        add(titlePane, CC().growX().pushX().minWidth("0"))
        add(textPane, CC().newline().skip().grow().push().minWidth("0").minHeight("0"))
    }
}