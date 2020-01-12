package net.ntworld.mergeRequestIntegrationIde.ui.panel

import com.intellij.ui.JBColor
import net.ntworld.mergeRequest.Commit
import net.ntworld.mergeRequestIntegration.util.DateTimeUtil
import net.ntworld.mergeRequestIntegrationIde.ui.Component
import java.awt.Dimension
import javax.swing.*
import javax.swing.event.ChangeListener

class CommitItemPanel(val commit: Commit, private val changeListener: ChangeListener) : Component {
    var myWholePanel: JPanel? = null
    var myWrapperPanel: JPanel? = null
    var mySelectedWrapperPanel: JPanel? = null
    var myInfoWrapperPanel: JPanel? = null
    var myTitleWrapperPanel: JPanel? = null
    var myInfoDetailsWrapperPanel: JPanel? = null
    var mySelected: JCheckBox? = null
    var myCommitHash: JTextField? = null
    var myAuthorAndTime: JLabel? = null
    var myTitle: JLabel? = null

    init {
        mySelected!!.isSelected = true
        myTitle!!.text = commit.message
        myAuthorAndTime!!.text = "${commit.authorName} · ${DateTimeUtil.toPretty(DateTimeUtil.toDate(commit.createdAt))}"
        myCommitHash!!.text = commit.id

        myWholePanel!!.border = BorderFactory.createMatteBorder(0, 0, 1, 0, JBColor.border())
        myWholePanel!!.maximumSize = Dimension(Int.MAX_VALUE, 60)
        mySelected!!.addChangeListener(changeListener)

        myWholePanel!!.background = JBColor.background()
        myWrapperPanel!!.background = JBColor.background()
        mySelectedWrapperPanel!!.background = JBColor.background()
        myInfoWrapperPanel!!.background = JBColor.background()
        myTitleWrapperPanel!!.background = JBColor.background()
        myInfoDetailsWrapperPanel!!.background = JBColor.background()
    }

    fun disable() {
        if (this.isSelected()) {
            mySelectedWrapperPanel!!.isVisible = false
        } else {
            myWholePanel!!.isVisible = false
        }
    }

    fun enable() {
        mySelectedWrapperPanel!!.isVisible = true
        myWholePanel!!.isVisible = true
    }

    fun isSelected(): Boolean = mySelected!!.isSelected

    fun isSelectable(selectable: Boolean, selected: Boolean? = null) {
        if (selectable) {
            mySelected!!.isEnabled = true
            if (null !== selected) {
                mySelected!!.isSelected = selected
            }
        } else {
            mySelected!!.isEnabled = false
            mySelected!!.toolTipText = "Cannot select this commit"
            mySelected!!.isSelected = false
        }
    }

    override fun createComponent(): JComponent = myWholePanel!!
}