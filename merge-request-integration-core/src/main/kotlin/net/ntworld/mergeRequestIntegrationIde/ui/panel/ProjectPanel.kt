package net.ntworld.mergeRequestIntegrationIde.ui.panel

import net.ntworld.mergeRequest.Project
import java.awt.Color
import javax.swing.*

class ProjectPanel(project: Project, isSelected: Boolean) {
    var myWrapper: JPanel? = null
    var myInner: JPanel? = null
    var myProjectName: JLabel? = null
    var myUrl: JLabel? = null

    init {
        myInner!!.border = BorderFactory.createLineBorder(Color(0, 0, 0, 0), 3)
        myProjectName!!.text = project.name
        myUrl!!.text = project.url
        setSelected(isSelected)
    }

    fun setSelected(value: Boolean) {
        if (!value) {
            myWrapper!!.border = BorderFactory.createLineBorder(Color(0, 0, 0, 0), 1)
        } else {
            myWrapper!!.border = BorderFactory.createLineBorder(Color(164, 55, 65), 1)
        }
    }

    fun getComponent(): JPanel = myWrapper!!
}
