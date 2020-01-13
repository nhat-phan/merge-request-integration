package net.ntworld.mergeRequestIntegrationIde.ui.panel

import com.intellij.ui.JBColor
import com.intellij.util.ui.UIUtil
import net.ntworld.mergeRequest.MergeRequestInfo
import net.ntworld.mergeRequestIntegration.util.DateTimeUtil
import net.ntworld.mergeRequestIntegrationIde.ui.Component
import java.awt.Color
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel

class MergeRequestItemPanel(private val mergeRequestInfo: MergeRequestInfo): Component {
    var myWholePanel: JPanel? = null
    var myWrapper: JPanel? = null
    var myTimeWrapper: JPanel? = null
    var myTitle: JLabel? = null
    var myCreated: JLabel? = null
    var myUpdated: JLabel? = null

    init {
        myTitle!!.text = mergeRequestInfo.title
        val created = DateTimeUtil.toDate(mergeRequestInfo.createdAt)
        myCreated!!.text = "Created ${DateTimeUtil.toPretty(created)}"
        myCreated!!.toolTipText = DateTimeUtil.formatDate(created)

        val updated = DateTimeUtil.toDate(mergeRequestInfo.updatedAt)
        myUpdated!!.text = "updated ${DateTimeUtil.toPretty(updated)}"
        myUpdated!!.toolTipText = DateTimeUtil.formatDate(updated)
    }

    fun changeStyle(selected: Boolean, hasFocus: Boolean) {
        val backgroundColor = UIUtil.getListBackground(selected, hasFocus)

        myWholePanel!!.background = backgroundColor
        myWrapper!!.background = backgroundColor
        myTimeWrapper!!.background = backgroundColor

        val foregroundColor = UIUtil.getListForeground(selected, hasFocus)
        myTitle!!.foreground = foregroundColor

        val foregroundColorOrGray = if (selected && hasFocus) foregroundColor else JBColor.gray
        myCreated!!.foreground = foregroundColorOrGray
        myUpdated!!.foreground = foregroundColorOrGray
    }

    override fun createComponent(): JComponent = myWholePanel!!
}