package net.ntworld.mergeRequestIntegrationIde.ui.panel

import net.ntworld.mergeRequest.UserInfo
import net.ntworld.mergeRequestIntegrationIde.ui.Component
import java.awt.Color
import javax.swing.Icon
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel

class UserInfoItemPanel(private val userInfo: UserInfo, private val icon: Icon? = null): Component {
    var myWholePanel: JPanel? = null
    var myWrapper: JPanel? = null
    var myName: JLabel? = null
    var myUsername: JLabel? = null
    var myIcon: JLabel? = null

    init {
        if (null === icon) {
            myIcon!!.isVisible = false
        } else {
            myIcon!!.icon = icon
            myIcon!!.isVisible = true
        }
        myName!!.text = userInfo.name
        myUsername!!.text = "@${userInfo.username}"
    }

    override fun createComponent(): JComponent {
        return myWholePanel!!
    }

    fun setBackground(color: Color) {
        myWholePanel!!.background = color
        myWrapper!!.background = color
    }
}