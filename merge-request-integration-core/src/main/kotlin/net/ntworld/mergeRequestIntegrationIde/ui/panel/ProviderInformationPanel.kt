package net.ntworld.mergeRequestIntegrationIde.ui.panel

import com.intellij.ide.BrowserUtil
import com.intellij.ui.JBColor
import net.ntworld.mergeRequest.ProjectVisibility
import net.ntworld.mergeRequest.ProviderData
import net.ntworld.mergeRequestIntegrationIde.ENTERPRISE_EDITION_URL
import net.ntworld.mergeRequestIntegrationIde.infrastructure.ApplicationService
import net.ntworld.mergeRequestIntegrationIde.ui.Component
import java.awt.Color
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel

class ProviderInformationPanel(
    private val applicationService: ApplicationService,
    private val providerData: ProviderData
): Component {
    var myWholePanel: JPanel? = null
    var myWrapperPanel: JPanel? = null
    var myLoggedUserWrapperPanel: JPanel? = null
    var myUrlWrapperPanel: JPanel? = null
    var myStatusWrapperPanel: JPanel? = null
    var myLicenseWarningWrapperPanel: JPanel? = null
    var myOpenURLButton: JButton? = null
    var myBuyButton: JButton? = null
    var myProjectName: JLabel? = null
    var myProjectUrl: JLabel? = null
    var myUserFullName: JLabel? = null
    var myUserUsername: JLabel? = null
    var myLocalRepository: JLabel? = null
    var myProjectVisibility: JLabel? = null
    var myLegalStatus: JLabel? = null
    var myLegalWarning: JLabel? = null
    var myWords1: JLabel? = null
    var myWords2: JLabel? = null
    var myWords3: JLabel? = null

    init {
        myProjectName!!.text = providerData.project.name
        myProjectUrl!!.text = providerData.project.url
        myUserFullName!!.text = providerData.currentUser.name
        myUserUsername!!.text = "@${providerData.currentUser.username}"
        myLocalRepository!!.text = providerData.repository
        myProjectVisibility!!.text = when (providerData.project.visibility) {
            ProjectVisibility.PUBLIC -> "Public"
            ProjectVisibility.PRIVATE -> "Private"
        }
        myOpenURLButton!!.addActionListener {
            BrowserUtil.open(providerData.project.url)
        }

        val isLegal = this.applicationService.isLegal(providerData)
        if (!isLegal) {
            myLegalStatus!!.text = "Illegal"
            myLegalStatus!!.foreground = Color(0xBA, 0x3F, 0x3C)
        } else {
            myLegalStatus!!.text = "Legal"
            myLegalStatus!!.foreground = Color(0x68, 0x84, 0x57)
        }
        myBuyButton!!.isVisible = !isLegal
        myLegalWarning!!.isVisible = !isLegal
        myWords1!!.isVisible = !isLegal
        myWords2!!.isVisible = !isLegal
        myWords3!!.isVisible = !isLegal

        myBuyButton!!.addActionListener {
            BrowserUtil.open(ENTERPRISE_EDITION_URL)
        }

        setBackground()
    }

    private fun setBackground() {
        myWholePanel!!.background = JBColor.background()
        myOpenURLButton!!.isOpaque = true
        myOpenURLButton!!.background = JBColor.background()
        myBuyButton!!.isOpaque = true
        myBuyButton!!.background = JBColor.background()
        myWrapperPanel!!.background = JBColor.background()
        myLoggedUserWrapperPanel!!.background = JBColor.background()
        myUrlWrapperPanel!!.background = JBColor.background()
        myStatusWrapperPanel!!.background = JBColor.background()
        myLicenseWarningWrapperPanel!!.background = JBColor.background()
    }

    override fun createComponent(): JComponent = myWholePanel!!
}