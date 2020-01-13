package net.ntworld.mergeRequestIntegrationIde.ui.panel

import com.intellij.openapi.util.IconLoader
import com.intellij.ui.JBColor
import com.intellij.util.ui.UIUtil
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequest.ProviderStatus
import net.ntworld.mergeRequestIntegrationIde.ui.Component
import net.ntworld.mergeRequestIntegrationIde.ui.util.ImageUtil
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel

class ProviderItemPanel(private val providerData: ProviderData) : Component {
    var myWholePanel: JPanel? = null
    var myWrapperPanel: JPanel? = null
    var myAvatarWrapperPanel: JPanel? = null
    var myUserInfoWrapperPanel: JPanel? = null
    var myInfoWrapper: JPanel? = null
    var myStatusWrapperPanel: JPanel? = null
    var myProjectName: JLabel? = null
    var myName: JLabel? = null
    var myUsername: JLabel? = null
    var myAvatar: JLabel? = null
    var myStatus: JLabel? = null

    init {
        myAvatar!!.icon = ImageUtil.loadIconFromUrl(providerData.project.avatarUrl, providerData.info.icon3xPath, 48)
        myProjectName!!.text = providerData.name
        myName!!.text = providerData.currentUser.name
        myUsername!!.text = "@${providerData.currentUser.username}"
        if (providerData.status == ProviderStatus.ERROR) {
            myStatus!!.icon = IconLoader.getIcon("/icons/exclamation.2x.svg", ProviderItemPanel::class.java)
            myStatus!!.toolTipText = "Cannot fetch data from this provider"
            myStatus!!.isVisible = true
        } else {
            myStatus!!.isVisible = false
        }
    }

    fun changeStyle(selected: Boolean, hasFocus: Boolean) {
        val backgroundColor = UIUtil.getListBackground(selected, hasFocus)

        myWholePanel!!.background = backgroundColor
        myWrapperPanel!!.background = backgroundColor
        myStatusWrapperPanel!!.background = backgroundColor
        myInfoWrapper!!.background = backgroundColor
        myAvatarWrapperPanel!!.background = backgroundColor
        myUserInfoWrapperPanel!!.background = backgroundColor

        val foregroundColor = UIUtil.getListForeground(selected, hasFocus)
        myProjectName!!.foreground = foregroundColor
        myName!!.foreground = foregroundColor
        myUsername!!.foreground = if (selected && hasFocus) foregroundColor else JBColor.gray
    }

    override fun createComponent(): JComponent = myWholePanel!!
}